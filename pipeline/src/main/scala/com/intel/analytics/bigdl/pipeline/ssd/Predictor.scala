/*
 * Copyright 2016 The BigDL Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.analytics.bigdl.pipeline.ssd

import com.intel.analytics.bigdl.Module
import com.intel.analytics.bigdl.dataset.Transformer
import com.intel.analytics.bigdl.pipeline.common.dataset.roiimage._
import com.intel.analytics.bigdl.models.utils.ModelBroadcast
import com.intel.analytics.bigdl.numeric.NumericFloat
import org.apache.spark.rdd.RDD

class Predictor(
    model: Module[Float],
    preProcessParam: PreProcessParam,
    postProcessParam: PostProcessParam) {

  val preProcessor =
    RoiImageResizer(Array(preProcessParam.resolution), resizeRois = false, isEqualResize = true) ->
      RoiImageNormalizer(preProcessParam.pixelMeanRGB) ->
      RoiimageToBatch(preProcessParam.batchSize, false)

  val postProcessor = new Postprocessor(postProcessParam)

  def predict(rdd: RDD[RoiByteImage]): RDD[Array[Target]] = {
    Predictor.predict(rdd, model, preProcessor, postProcessor)
  }
}

object Predictor {
  def predict(rdd: RDD[RoiByteImage],
              model: Module[Float],
              preProcessor: Transformer[RoiByteImage, MiniBatch[Float]],
              postProcessor: Postprocessor
             ): RDD[Array[Target]] = {
    model.evaluate()
    val broadcastModel = ModelBroadcast().broadcast(rdd.sparkContext, model)
    val broadpostprocessor = rdd.sparkContext.broadcast(postProcessor)
    rdd.mapPartitions(preProcessor(_)).mapPartitions(dataIter => {
      val localModel = broadcastModel.value()
      val localPostProcessor = broadpostprocessor.value.clone()
      dataIter.map(batch => {
        val result = localModel.forward(batch.data).toTable
        localPostProcessor.process(result, batch.imInfo)
      }).flatten
    })
  }
}

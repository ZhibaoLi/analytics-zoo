/*
 * Copyright 2018 Analytics Zoo Authors.
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

package com.intel.analytics.zoo.pipeline.api.keras.layers

import com.intel.analytics.bigdl.nn.abstractnn.{AbstractModule, Activity}
import com.intel.analytics.bigdl.nn.keras.KerasLayer
import com.intel.analytics.bigdl.tensor.Tensor
import com.intel.analytics.bigdl.tensor.TensorNumericMath.TensorNumeric
import com.intel.analytics.bigdl.utils.{Shape, T, Table}
import com.intel.analytics.zoo.pipeline.api.Net
import com.intel.analytics.zoo.pipeline.api.keras.layers.LayerWrapperByForward
import com.intel.analytics.zoo.pipeline.api.keras.layers.utils.KerasUtils

import scala.reflect.ClassTag

class SelectTable[T: ClassTag](index: Int, inputShape: Shape = null)
  (implicit ev: TensorNumeric[T])
  extends LayerWrapperByForward[T](KerasUtils.addBatch(inputShape)) {

  override def doBuild(inputShape: Shape): AbstractModule[Activity, Activity, T] = {
    val layer = com.intel.analytics.bigdl.nn.SelectTable(index)
    layer.asInstanceOf[AbstractModule[Activity, Activity, T]]
  }
}

object SelectTable {
  def apply[@specialized(Float, Double) T: ClassTag](index: Int, inputShape: Shape = null)
    (implicit ev: TensorNumeric[T]) : SelectTable[T] = {
    new SelectTable[T](index, inputShape)
  }
}
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

package com.intel.analytics.zoo.models.transformer

import com.intel.analytics.bigdl.tensor.Tensor
import com.intel.analytics.bigdl.utils.RandomGenerator._
import com.intel.analytics.bigdl.utils.{Shape, T}
import com.intel.analytics.zoo.models.common.ZooModel
import com.intel.analytics.zoo.pipeline.api.autograd.Variable
import com.intel.analytics.zoo.pipeline.api.keras.ZooSpecHelper
import com.intel.analytics.zoo.pipeline.api.keras.serializer.ModuleSerializationTest
import com.intel.analytics.zoo.pipeline.api.keras.layers.Conv1D
import com.intel.analytics.zoo.pipeline.api.keras.models.Model

class TransformerSpec extends ZooSpecHelper {
  "Transformer model" should "be able to work" in {
    val model = Transformer[Float](vocab = 100, embeddingSize = 768)
    val input = Tensor[Float](Array(2, 2, 77, 2)).rand().resize(4, 77, 2)
    val gradOutput = Tensor[Float](4, 77, 768).rand()
    val output = model.forward(input)
    val gradInput = model.backward(input, gradOutput)
  }

  "Transformer model" should "be able to generate correct result" in {
    RNG.setSeed(42)
    val model = Transformer[Float](vocab = 10, embeddingSize = 4, nCtx = 2, nHead = 2)
    val data = Array[Float](6, 3, 7, 4, 6, 9, 2, 6, 7, 4, 3, 7, 7, 2, 5, 4)
    val wb = model.parameters()._1

    val embedingW = Tensor[Float](Array[Float](0.0035f,  0.0210f,  0.0001f, -0.0015f,
    0.0129f,  0.0115f,  0.0117f, -0.0004f,
    -0.0183f,  0.0297f, -0.0182f, -0.0106f,
    -0.0161f,  0.0103f, -0.0143f,  0.0044f,
     0.0113f,  0.0372f,  0.0209f, -0.0173f,
     0.0167f, -0.0063f,  0.0054f,  0.0017f,
     0.0195f, -0.0203f, -0.0108f, -0.0088f,
    -0.0063f, -0.0026f, -0.0143f, -0.0010f,
     0.0404f,  0.0051f,  0.0187f,  0.0142f,
    -0.0006f,  0.0020f,  0.0269f,  0.0143f),
      Array(10, 4))
    wb(0).set(embedingW)

    val conv1W = Tensor[Float](Array[Float](-0.0167f, -0.0184f,  0.0362f,  0.0032f,  0.0073f,
      0.0035f,  0.0277f, -0.0089f, -0.0240f, 0.0142f, -0.0215f, 0.0107f,
      0.0235f,  0.0112f, -0.0091f, -0.0154f, 0.0029f,  0.0046f, 0.0002f, -0.0028f, 0.0039f,
      -0.0229f,  0.0068f, 0.0340f, 0.0563f, 0.0072f, -0.0018f, 0.0092f, -0.0113f, 0.0211f,
      -0.0294f,  0.0287f, 0.0146f, -0.0142f, -0.0120f, 0.0192f, 0.0081f, -0.0271f, -0.0100f,
      0.0095f, -0.0040f, 0.0254f,  0.0245f, 0.0020f, 0.0348f, -0.0271f, 0.0044f, 0.0111f),
      Array(1, 1, 1, 4, 12))
    wb(2).set(conv1W)

    val conv2W = Tensor[Float](Array[Float](-0.0136f,  0.0115f,  0.0038f, -0.0072f,
    -0.0063f,  0.0118f, -0.0178f,  0.0082f,
    -0.0197f,  0.0025f,  0.0070f,  0.0123f,
    -0.0034f,  0.0047f,  0.0807f,  0.0256f), Array(1, 1, 1, 4, 4))
    wb(4).set(conv2W)

    val conv3W = Tensor[Float](Array[Float](0.0206f, -0.0141f,  0.0203f, -0.0066f, 0.0104f,
      0.0078f, -0.0116f, -0.0034f, -0.0115f,  0.0101f, -0.0095f, -0.0098f,  0.0054f, -0.0113f,
      0.0136f, 0.0088f, -0.0072f, -0.0012f,  0.0015f,  0.0164f,  0.0296f,  0.0069f, -0.0285f,
      -0.0023f, 0.0044f, -0.0009f, -0.0287f, -0.0113f, -0.0085f,  0.0053f, -0.0288f,  0.0104f,
      0.0208f, -0.0080f, -0.0459f,  0.0100f, -0.0085f, -0.0267f, -0.0039f,  0.0131f, -0.0061f,
      -0.0066f, -0.0196f,  0.0039f, -0.0331f,  0.0136f,  0.0292f, -0.0062f, 0.0193f, -0.0062f,
      0.0114f,  0.0224f, -0.0259f,  0.0010f, -0.0117f, -0.0078f, 0.0196f, -0.0128f, -0.0098f,
      0.0042f, -0.0232f, -0.0193f, -0.0075f,  0.0161f), Array(1, 1, 1, 4, 16))
    wb(8).set(conv3W)

    val conv4W = Tensor[Float](Array[Float](0.0143f,  0.0307f, -0.0290f, -0.0157f,
    -0.0191f, -0.0250f, -0.0150f, -0.0118f,
    -0.0307f, -0.0145f,  0.0093f,  0.0133f,
    -0.0009f,  0.0047f, -0.0141f, -0.0143f,
    -0.0032f, -0.0085f,  0.0189f, -0.0037f,
     0.0212f,  0.0042f, -0.0116f,  0.0065f,
     0.0052f, -0.0152f, -0.0409f, -0.0306f,
     0.0081f,  0.0126f,  0.0063f, -0.0007f,
     0.0261f,  0.0098f,  0.0227f, -0.0071f,
     0.0072f,  0.0400f,  0.0133f,  0.0141f,
     0.0004f, -0.0166f, -0.0216f, -0.0157f,
     0.0101f,  0.0016f,  0.0089f, -0.0145f,
    -0.0092f, -0.0013f, -0.0273f,  0.0066f,
    -0.0197f,  0.0060f,  0.0036f, -0.0026f,
    -0.0315f,  0.0450f,  0.0200f,  0.0273f,
     0.0127f,  0.0081f,  0.0068f, -0.0044f), Array(1, 1, 1, 16, 4))
    wb(10).set(conv4W)

    val input = Tensor[Float](data, Array(4, 2, 2))
    val output = model.forward(input).toTensor[Float]

    val expect = Tensor[Float](Array[Float](1.1891f, -0.0895f, -1.5431f,  0.4436f,
    -0.1075f,  1.4737f, -0.0185f, -1.3477f,
    0.9136f, -1.6274f,  0.7188f, -0.0050f,
    0.6926f,  1.2211f, -1.2716f, -0.6420f,
    -0.1062f,  1.4747f, -0.0218f, -1.3466f,
    -0.7912f,  1.1205f, -1.1806f,  0.8513f,
    -0.6202f,  1.6365f, -0.9668f, -0.0495f,
    0.5538f,  0.7061f,  0.4657f, -1.7256f), Array(4, 2, 4))
    require(output.almostEqual(expect, 7e-3) == true)

    val gradOutput = Tensor[Float](Array[Float](0.6325f, 0.3265f, 0.5406f, 0.9662f,
    0.7304f, 0.0667f, 0.6985f, 0.9746f,
    0.6315f, 0.8352f, 0.9929f, 0.4234f,
    0.6038f, 0.1525f, 0.3970f, 0.8703f,
    0.7563f, 0.1836f, 0.0991f, 0.1583f,
    0.0066f, 0.1142f, 0.3764f, 0.8374f,
    0.5837f, 0.1197f, 0.0989f, 0.7487f,
    0.1281f, 0.4384f, 0.7399f, 0.2686f), Array(4, 2, 4))

    model.backward(input, gradOutput)
    val t = model.parameters()._2
    val t2 = 0
  }

  "Utils tril" should "be able to work" in {
    val data = Tensor.ones[Float](3, 3)
    Utils.tril(data)
    val expect = Array[Float](1, 0, 0, 1, 1, 0, 1, 1, 1)
    val res = data.storage().array()
    require(expect.deep == res.deep)

    val data2 = Tensor.ones[Float](4, 6)
    Utils.tril(data2)
    val expect2 = Array[Float](1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0,
      1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0)
    val res2 = data2.storage().array()
    require(expect2.deep == res2.deep)
  }

  "Conv1D" should "be generate the same result with pytorch-openai conv1d" in {
    val x = Variable[Float](Shape(3, 5))
    val m = Conv1D[Float](3, 1)
    val o = m.from(x)
    val model = Model[Float](input = x, output = o)
    val data = Array[Float](2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
      20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31)

    val w = Tensor[Float](Array[Float](30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43,44),
      Array(1, 1, 1, 5, 3))
    model.setWeightsBias(Array(w, Tensor[Float](3)))
    val output2 = model.forward(Tensor[Float](data, Array(2, 3, 5))).toTensor[Float]

    val expect = Tensor[Float](Array[Float](750,770,790,1650,1695,1740,2550,2620,2690,3450,
      3545,3640,4350,4470,4590,5250,5395,5540), Array(2, 3, 3))
    require(output2.almostEqual(expect, 1e-8) == true)
  }
}

class TransformerSerialTest extends ModuleSerializationTest {
  override def test(): Unit = {
    val model = Transformer[Float]()
    val input = Tensor[Float](Array(2, 2, 77, 2)).rand()
    ZooSpecHelper.testZooModelLoadSave(
      model.asInstanceOf[ZooModel[Tensor[Float], Tensor[Float], Float]],
      input, Transformer.loadModel[Float])
  }
}

/*
 * Copyright 2016 Dennis Vriend
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

package com.github.dnvriend.streams.stage.simple

import akka.stream.testkit.scaladsl.TestSink
import com.github.dnvriend.streams.TestSpec

class MapConcatTest extends TestSpec {
  /**
   * Transform each input element into an `Iterable` of output elements that is
   * then flattened into the output stream.
   *
   * The returned `Iterable` MUST NOT contain `null` values,
   * as they are illegal as stream elements - according to the Reactive Streams specification.
   *
   * - Emits when: the mapping function returns an element or there are still remaining elements
   * from the previously calculated collection
   *
   * - Backpressures when: downstream backpressures or there are still remaining elements from the
   * previously calculated collection
   *
   * - Completes when: upstream completes and all remaining elements has been emitted
   *
   * - Cancels when: downstream cancels
   */

  "MapConcat" should "transform each input element into an 'iterable' of output elements that is then flattened into the output stream" in {
    withIterator() { src ⇒
      src.take(3)
        .mapConcat(e ⇒ List(e, e, e))
        .runWith(TestSink.probe[Int])
        .request(Integer.MAX_VALUE)
        .expectNext(0, 0, 0, 1, 1, 1, 2, 2, 2)
        .expectComplete()
    }
  }

  it should "flatten two lists" in {
    withIterator() { src ⇒
      src.take(5)
        .grouped(3)
        .mapConcat(identity)
        .runWith(TestSink.probe[Int])
        .request(Integer.MAX_VALUE)
        .expectNext(0, 1, 2, 3, 4)
        .expectComplete()
    }
  }

  it should "flatten two sequences" in {
    withIterator() { src ⇒
      src.take(10)
        .splitWhen(_ < 3)
        .concatSubstreams
        .runForeach(println)
    }
  }
}

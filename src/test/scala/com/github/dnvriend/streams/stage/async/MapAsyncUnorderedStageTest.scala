/*
 * Copyright 2015 Dennis Vriend
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

package com.github.dnvriend.streams.stage.async

import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import com.github.dnvriend.streams.TestSpec

import scala.concurrent.Future

class MapAsyncUnorderedStageTest extends TestSpec {
  /**
   * Transform this stream by applying the given function to each of the elements
   * as they pass through this processing step. The function returns a `Future` and the
   * value of that future will be emitted downstreams.
   *
   * As many futures as requested elements by downstream may run in parallel and each processed element
   * will be emitted dowstream as soon as it is ready, i.e. it is possible that the elements are not
   * emitted downstream in the same order as received from upstream.
   *
   * If the group by function `f` throws an exception or if the `Future` is completed
   * with failure and the supervision decision is [[akka.stream.Supervision.Stop]]
   * the stream will be completed with failure.
   *
   * If the group by function `f` throws an exception or if the `Future` is completed
   * with failure and the supervision decision is [[akka.stream.Supervision.Resume]] or
   * [[akka.stream.Supervision.Restart]] the element is dropped and the stream continues.
   *
   * - Emits when: any of the Futures returned by the provided function complete
   * - Backpressures when: the number of futures reaches the configured parallelism and the downstream backpressures
   * - Completes when: upstream completes and all futures has been completed and all elements has been emitted
   * - Cancels when: downstream cancels
   */

  "MapAsyncUnordered" should "transform the stream by applying the function to each element" in {
    withIterator() { src ⇒
      src.take(10)
        .mapAsyncUnordered(4)(num ⇒ Future(num * 2))
        .runWith(TestSink.probe[Int])
        .request(11)
        .expectNextUnordered(0, 2, 4, 6, 8, 10, 12, 14, 16, 18)
        .expectComplete()
    }
  }
}

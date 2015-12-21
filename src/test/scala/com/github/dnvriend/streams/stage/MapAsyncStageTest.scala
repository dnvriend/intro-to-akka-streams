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

package com.github.dnvriend.streams.stage

import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import com.github.dnvriend.streams.TestSpec

import scala.concurrent.Future

class MapAsyncStageTest extends TestSpec {
  /**
   * Transform this stream by applying the given function to each of the elements
   * as they pass through this processing step. The function returns a `Future` and the
   * value of that future will be emitted downstream. The number of Futures
   * that shall run in parallel is given as the first argument to ``mapAsync``.
   * These Futures may complete in any order, but the elements that
   * are emitted downstream are in the same order as received from upstream.
   *
   * If the group by function `f` throws an exception or if the `Future` is completed
   * with failure and the supervision decision is [[akka.stream.Supervision.Stop]]
   * the stream will be completed with failure.
   *
   * If the group by function `f` throws an exception or if the `Future` is completed
   * with failure and the supervision decision is [[akka.stream.Supervision.Resume]] or
   * [[akka.stream.Supervision.Restart]] the element is dropped and the stream continues.
   *
   * - Emits when: the Future returned by the provided function finishes for the next element in sequence
   *
   * - Backpressures when: the number of futures reaches the configured parallelism and the downstream
   * backpressures or the first future is not completed
   *
   * - Completes when: upstream completes and all futures has been completed and all elements has been emitted
   * - Cancels when: downstream cancels
   */

  "MapAsync" should "transform the stream by applying the function to each element" in {
    Source(() ⇒ Iterator from 0)
      .take(3)
      .mapAsync(2)(num ⇒ Future(num * 2))
      .runWith(TestSink.probe[Int])
      .request(4)
      .expectNext(0, 2, 4)
      .expectComplete()
  }
}

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

class FoldStageTest extends TestSpec {
  /**
   * Similar to `scan` but only emits its result when the upstream completes,
   * after which it also completes. Applies the given function towards its current and next value,
   * yielding the next current value.
   *
   * If the function `f` throws an exception and the supervision decision is
   * [[akka.stream.Supervision.Restart]] current value starts at `zero` again
   * the stream will continue.
   *
   * - Emits when: upstream completes
   * - Backpressures when: downstream backpressures
   * - Completes when: upstream completes
   * - Cancels when: downstream cancels
   */

  "Fold" should "emit only an element when the upstream completes" in {
    withIterator() { src ⇒
      src.take(4)
        .fold(0) { (c, _) ⇒ c + 1 }
        .runWith(TestSink.probe[Int])
        .request(Int.MaxValue)
        .expectNext(4)
        .expectComplete()
    }
  }
}

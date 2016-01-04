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

class ScanStageTest extends TestSpec {

  /**
   * Similar to `fold` but is not a terminal operation,
   * emits its current value which starts at `zero` and then
   * applies the current and next value to the given function `f`,
   * emitting the next current value.
   *
   * If the function `f` throws an exception and the supervision decision is
   * [[akka.stream.Supervision.Restart]] current value starts at `zero` again
   * the stream will continue.
   *
   * - Emits when: the function scanning the element returns a new element
   * - Backpressures when: downstream backpressures
   * - Completes when: upstream completes
   * - Cancels when: downstream cancels
   */

  "Scan" should "do the same as fold, but emits the next current value to the stream" in {
    withIterator() { src ⇒
      src.take(4)
        .scan(0) { (c, _) ⇒ c + 1 }
        .runWith(TestSink.probe[Int])
        .request(Integer.MAX_VALUE)
        .expectNext(0, 1, 2, 3, 4)
        .expectComplete()
    }
  }
}

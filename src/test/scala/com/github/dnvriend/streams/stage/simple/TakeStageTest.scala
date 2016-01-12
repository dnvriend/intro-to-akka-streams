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

class TakeStageTest extends TestSpec {
  /**
   * Terminate processing (and cancel the upstream publisher) after the given
   * number of elements. Due to input buffering some elements may have been
   * requested from upstream publishers that will then not be processed downstream
   * of this step.
   *
   * The stream will be completed without producing any elements if `n` is zero
   * or negative.
   *
   * - Emits when: the specified number of elements to take has not yet been reached
   * - Backpressures when: downstream backpressures
   * - Completes when: the defined number of elements has been taken or upstream completes
   * - Cancels when: the defined number of elements has been taken or downstream cancels
   */

  "Take" should "emit only 'n' number of elements and then complete" in {
    withIterator() { src ⇒
      src.take(3)
        .runWith(TestSink.probe[Int])
        .request(Int.MaxValue)
        .expectNext(0, 1, 2)
        .expectComplete()
    }
  }
}

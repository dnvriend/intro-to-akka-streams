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

class DropStageTest extends TestSpec {
  /**
   * Discard the given number of elements at the beginning of the stream.
   * No elements will be dropped if `n` is zero or negative.
   *
   * - Emits when: the specified number of elements has been dropped already
   * - Backpressures when: the specified number of elements has been dropped and downstream backpressures
   * - Completes when: upstream completes
   * - Cancels when: downstream cancels
   */

  "Drop" should "discard the given number of elements at the beginning of the stream" in {
    withIterator() { src ⇒
      src.take(10)
        .drop(5)
        .runWith(TestSink.probe[Int])
        .request(Int.MaxValue)
        .expectNext(5, 6, 7, 8, 9)
        .expectComplete()
    }
  }
}

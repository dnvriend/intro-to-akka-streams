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

package com.github.dnvriend.streams.stage.simple

import akka.stream.testkit.scaladsl.TestSink
import com.github.dnvriend.streams.TestSpec

class FilterStageTest extends TestSpec {
  /**
   * Only pass on those elements that satisfy the given predicate.
   *
   * - Emits when: the given predicate returns true for the element
   * - Backpressures when: the given predicate returns true for the element and downstream backpressures
   * - Completes when: upstream completes
   * - Cancels when: downstream cancels
   */

  "Filter a sequence of numbers for even numbers" should "emit only even numbers" in {
    withIterator() { src ⇒
      src.take(10)
        .filter(_ % 2 == 0)
        .runWith(TestSink.probe[Int])
        .request(10)
        .expectNext(0, 2, 4, 6, 8)
        .expectComplete()
    }
  }
}

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

import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import com.github.dnvriend.streams.TestSpec

class DropWhileStageTest extends TestSpec {
  /**
   * Discard elements at the beginning of the stream while predicate is true.
   * All elements will be taken after predicate returns false first time.
   *
   * - Emits when: predicate returned false and for all following stream elements
   * - Backpressures when: predicate returned false and downstream backpressures
   * - Completes when: upstream completes
   * - Cancels when: downstream cancels
   */

  "DropWhile" should "discard elements while the predicate is true, else it emits elements" in {
    Source(() ⇒ Iterator from 0)
      .take(10)
      .dropWhile(_ < 5)
      .runWith(TestSink.probe[Int])
      .request(10)
      .expectNext(5, 6, 7, 8, 9)
      .expectComplete()
  }
}

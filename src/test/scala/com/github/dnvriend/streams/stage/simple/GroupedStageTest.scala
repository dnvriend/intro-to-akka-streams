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

class GroupedStageTest extends TestSpec {

  /**
   * Chunk up this stream into groups of the given size, with the last group
   * possibly smaller than requested due to end-of-stream.
   *
   * `n` must be positive, otherwise IllegalArgumentException is thrown.
   *
   * - Emits when: the specified number of elements has been accumulated or upstream completed
   * - Backpressures when: a group has been assembled and downstream backpressures
   * - Completes when: upstream completes
   * - Cancels when: downstream cancels
   */

  "Grouping a stream of numbers in sequences of three" should "result in two sequences" in {
    withIterator() { src ⇒
      src.take(5)
        .grouped(3)
        .runWith(TestSink.probe[Seq[Int]])
        .request(2)
        .expectNext(List(0, 1, 2), List(3, 4))
        .expectComplete()
    }
  }
}

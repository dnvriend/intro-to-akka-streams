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

class CollectStageTest extends TestSpec {
  /**
   * Transform this stream by applying the given partial function to each of the elements
   * on which the function is defined as they pass through this processing step.
   * Non-matching elements are filtered out.
   *
   * - Emits when: the provided partial function is defined for the element
   * - Backpressures when: the partial function is defined for the element and downstream backpressures
   * - Completes when: upstream completes
   * - Cancels when: downstream cancels
   */

  it should "emit only elements on which the partial function is defined" in {
    withIterator() { src ⇒
      src.take(10)
        .collect {
          case e if e < 5 ⇒ e
        }
        .runWith(TestSink.probe[Int])
        .request(Int.MaxValue)
        .expectNext(0, 1, 2, 3, 4)
        .expectComplete()
    }
  }

  it should "transform the stream by applying the partial function" in {
    withIterator() { src ⇒
      src.take(10)
        .collect {
          case e if e < 5           ⇒ e.toString
          case e if e >= 5 && e < 8 ⇒ (e * 2).toString
        }
        .runWith(TestSink.probe[String])
        .request(Int.MaxValue)
        .expectNext("0", "1", "2", "3", "4", "10", "12", "14")
        .expectComplete()
    }
  }

  it should "transform the stream by applying the partial function for each element" in {
    withIterator() { src ⇒
      src.take(10)
        .collect {
          case e if e < 5           ⇒ e.toString
          case e if e >= 5 && e < 8 ⇒ (e * 2).toString
          case _                    ⇒ "UNKNOWN"
        }
        .runWith(TestSink.probe[String])
        .request(Int.MaxValue)
        .expectNext("0", "1", "2", "3", "4", "10", "12", "14", "UNKNOWN", "UNKNOWN")
        .expectComplete()
    }
  }
}

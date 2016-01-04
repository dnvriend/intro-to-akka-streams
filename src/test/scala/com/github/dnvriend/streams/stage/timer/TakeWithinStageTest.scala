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

package com.github.dnvriend.streams.stage.timer

import akka.stream.testkit.scaladsl.TestSink
import com.github.dnvriend.streams.TestSpec

import scala.concurrent.duration._

class TakeWithinStageTest extends TestSpec {
  /**
   * Terminate processing (and cancel the upstream publisher) after the given
   * duration. Due to input buffering some elements may have been
   * requested from upstream publishers that will then not be processed downstream
   * of this step.
   *
   * Note that this can be combined with `take` to limit the number of elements
   * within the duration.
   *
   * - Emits when: an upstream element arrives
   * - Backpressures when: downstream backpressures
   * - Completes when: upstream completes or timer fires
   * - Cancels when: downstream cancels or timer fires
   */

  "TakeWithin" should "take elements in the duration window, when the window has passed, the stream completes" in {
    withIterator() { src ⇒
      src.takeWithin(500.millis)
        .map { e ⇒ Thread.sleep(200); e }
        .runWith(TestSink.probe[Int])
        .request(5)
        .expectNext(0, 1, 2, 3, 4)
        .expectComplete()
    }
  }
}

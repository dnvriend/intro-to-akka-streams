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

class TakeWhileStageTest extends TestSpec {
  /**
   * Terminate processing (and cancel the upstream publisher) after predicate
   * returns false for the first time. Due to input buffering some elements may have been
   * requested from upstream publishers that will then not be processed downstream
   * of this step.
   *
   * The stream will be completed without producing any elements if predicate is false for
   * the first stream element.
   *
   * - Emits when: the predicate is true
   * - Backpressures when: downstream backpressures
   * - Completes when: predicate returned false or upstream completes
   * - Cancels when predicate returned false or downstream cancels
   */

  "TakeWhile" should "emit elements while the predicate is true, and completes when the predicate is false" in {
    Source(() ⇒ Iterator from 0)
      .takeWhile(_ < 5)
      .runWith(TestSink.probe[Int])
      .request(6)
      .expectNext(0, 1, 2, 3, 4)
      .expectComplete()
  }
}

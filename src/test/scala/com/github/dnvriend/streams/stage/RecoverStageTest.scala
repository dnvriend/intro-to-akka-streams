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

package com.github.dnvriend.streams.stage

import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import com.github.dnvriend.streams.TestSpec

class RecoverStageTest extends TestSpec {
  /**
   * Recover allows to send last element on failure and gracefully complete the stream
   *
   * Note:
   * Since the underlying failure signal onError arrives out-of-band, it might jump over existing elements.
   * This stage can recover the failure signal, but not the skipped elements, which will be dropped.
   *
   * - Emits when: element is available from the upstream or upstream is failed and pf returns an element
   * - Backpressures when: downstream backpressures
   * - Completes when: upstream completes or upstream failed with exception pf can handle
   * - Cancels when: downstream cancels
   *
   */

  "Recover" should "emits / forward received elements for non-error messages / normal operation" in {
    Source(() ⇒ Iterator from 0)
      .take(3)
      .recover {
        case e: RuntimeException ⇒ 1000
      }
      .runWith(TestSink.probe[Int])
      .request(3)
      .expectNext(0, 1, 2)
      .expectComplete()
  }

  it should "emit 1000 when the stream fails thus recover the last element, afterwards the stream completes" in {
    Source(() ⇒ Iterator from 0)
      .take(3)
      .collect {
        case 1 ⇒ throw new RuntimeException("Forced exception")
        case e ⇒ e
      }
      .recover {
        case e: RuntimeException ⇒ 1000
      }
      .runWith(TestSink.probe[Int])
      .request(3)
      .expectNextUnordered(0, 1000)
      .expectComplete()
  }
}

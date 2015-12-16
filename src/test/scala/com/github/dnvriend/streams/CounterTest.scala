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

package com.github.dnvriend.streams

import akka.stream.Attributes
import akka.stream.scaladsl.{ Flow, Source }

class CounterTest extends TestSpec {

  /**
   * A single inputBuffer flow
   */
  val single = Flow[Int].withAttributes(Attributes.inputBuffer(initial = 1, max = 1))

  def debug[T] = Flow[T].map { x ⇒ println(x); x }

  val fastSrc =
    Source(() ⇒ Iterator from 0)
      .named("fast_source") // the AkkaFlowMaterializer will create actors,
  // every stage (step) in the flow will be an Actor
  // `named` sets the name of the actor

  val toTenSrc = Source(1 to 10)

  "IteratorSource" should "count to ten" in {
    fastSrc
      .via(single)
      .take(10)
      .via(debug)
      .runFold(0)((c, _) ⇒ c + 1)
      .futureValue shouldBe 10
  }

  it should "log elements using implicit LoggingAdapter" in {
    toTenSrc
      .log("one_to_ten")
      .runForeach(_ ⇒ ())
  }

}

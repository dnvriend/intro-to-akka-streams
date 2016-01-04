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

package com.github.dnvriend.streams.streammaterialization

import akka.stream.scaladsl.Sink
import com.github.dnvriend.streams.TestSpec

class StreamMaterializationTest extends TestSpec {

  /**
   * When constructing flows and graphs in Akka Streams think of them as preparing a blueprint, an execution plan.
   * Stream materialization is the process of taking a stream description (the graph) and allocating all the necessary
   * resources it needs in order to run.
   *
   * In the case of Akka Streams this often means starting up Actors which power the processing, but is not restricted
   * to that - it could also mean opening files or socket connections etc. – depending on what the stream needs.
   */

  /**
   * Materialization is triggered at so called "terminal operations". Most notably this includes the various forms
   * of the run() and runWith() methods defined on flow elements as well as a small number of special syntactic sugars
   * for running with well-known sinks, such as runForeach(el => ) (being an alias to runWith(Sink.foreach(el => )).
   */

  "Stream Materialization" should "be triggered using runFold" in {
    withIterator() { src ⇒
      src.take(10)
        .runFold(0) { (c, _) ⇒ c + 1 }
        .futureValue shouldBe 10
    }
  }

  it should "be triggered using runWith" in {
    withIterator() { src ⇒
      src.take(10)
        .runForeach(_ ⇒ ())
        .futureValue shouldBe ()
    }
  }

  it should "be triggered using runWith (which takes a sink shape)" in {
    withIterator() { src ⇒
      src.take(10)
        .runWith(Sink.foreach(_ ⇒ ()))
        .futureValue shouldBe ()
    }
  }
}

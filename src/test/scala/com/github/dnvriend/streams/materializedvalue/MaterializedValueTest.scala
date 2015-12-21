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

package com.github.dnvriend.streams.materializedvalue

import akka.actor.Cancellable
import akka.stream.scaladsl.{ Flow, Source }
import com.github.dnvriend.streams.TestSpec

import scala.concurrent.Promise

class MaterializedValueTest extends TestSpec {

  /**
   * Every processing stage in Akka Streams can provide a materialized value after being materialized.
   *
   * It is necessary to somehow express how these values should be composed to a final value when we plug
   * these stages together. For this, many combinator methods have variants that take an additional argument,
   * a function, that will be used to combine the resulting values.
   */

  // an empty source that can be shut down explicitly from the outside
  val source: Source[Int, Promise[Unit]] = Source.lazyEmpty[Int]

  //  val flow = Flow[Int, Int].map { _ => 10}

  "" should "" in {
  }
}

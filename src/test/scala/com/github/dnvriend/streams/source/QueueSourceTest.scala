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

package com.github.dnvriend.streams.source

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{ Keep, Sink, Source, SourceQueueWithComplete }
import com.github.dnvriend.streams.TestSpec

import scala.collection.immutable._
import scala.concurrent.Future

class QueueSourceTest extends TestSpec {
  it should "queue a b and c and return Seq(a, b, c)" in {
    val (queue: SourceQueueWithComplete[String], xs: Future[Seq[String]]) =
      Source.queue[String](Int.MaxValue, OverflowStrategy.backpressure).toMat(Sink.seq)(Keep.both).run()

    queue.offer("a").toTry should be a 'success // offer 'a' to stream
    queue.offer("b").toTry should be a 'success // b
    queue.offer("c").toTry should be a 'success // and c

    // complete the queue
    queue.complete()
    queue.watchCompletion().toTry should be a 'success

    // get the results of the stream
    xs.futureValue shouldEqual Seq("a", "b", "c")
    xs.futureValue should not equal Seq("c", "b", "a")
  }
}

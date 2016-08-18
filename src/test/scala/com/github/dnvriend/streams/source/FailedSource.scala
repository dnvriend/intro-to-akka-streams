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

import akka.Done
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{ Keep, Merge, Sink, Source, SourceQueueWithComplete }
import com.github.dnvriend.streams.TestSpec

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.collection.immutable._

class FailedSource extends TestSpec {
  it should "fail the stream" in {
    Source.failed[Int](new RuntimeException("test error")).testProbe { tp ⇒
      tp.request(Long.MaxValue)
      tp.expectError()
    }
  }

  it should "complete a stream" in {
    val (queue: SourceQueueWithComplete[Int], done: Future[Done]) = Source.queue[Int](1, OverflowStrategy.dropNew)
      .toMat(Sink.ignore)(Keep.both).run
    queue.complete()
    done.toTry should be a 'success
  }

  it should "complete a stream normally" in {
    val (queue: SourceQueueWithComplete[String], done: Future[Done]) = Source.queue[String](1, OverflowStrategy.dropNew).flatMapConcat {
      case "stop" ⇒ Source.failed(new RuntimeException("test error"))
      case str    ⇒ Source.single(str)
    }.toMat(Sink.seq)(Keep.both).run

    Thread.sleep(3000)
    queue.offer("foo").futureValue
    queue.offer("bar").futureValue
    queue.complete()
    done.futureValue shouldBe List("foo", "bar")
  }

  it should "force stop a stream with an error" in {
    val (queue: SourceQueueWithComplete[String], done: Future[Done]) = Source.queue[String](1, OverflowStrategy.dropNew).flatMapConcat {
      case "stop" ⇒ Source.failed(new RuntimeException("test error"))
      case str    ⇒ Source.single(str)
    }.toMat(Sink.seq)(Keep.both).run

    Thread.sleep(3000)
    queue.offer("stop").futureValue
    done.toTry should be a 'failure
  }

}

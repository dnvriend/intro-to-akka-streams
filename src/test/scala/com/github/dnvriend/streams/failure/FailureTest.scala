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

package com.github.dnvriend.streams.failure

import akka.stream.testkit.scaladsl.TestSink
import com.github.dnvriend.streams.TestSpec

import scala.concurrent.Future

class FailureTest extends TestSpec {
  def failedFuture = (_: Any) ⇒ Future.failed(new Throwable("Failure"))

  it should "fail a stream with a Future.failed" in {
    withIterator() { src ⇒
      src.take(5)
        .mapAsync(1)(failedFuture)
        .runWith(TestSink.probe[Seq[Int]])
        .request(Integer.MAX_VALUE)
        .expectError()
    }
  }

  it should "fail the resulting future with a Future.failed" in {
    withIterator() { src ⇒
      src.take(5).mapAsync(1)(failedFuture).runForeach(_ ⇒ ()).toTry should be a 'failure
    }
  }

  def throwException = (_: Any) ⇒ throw new RuntimeException("Failure")

  it should "fail a stream when throwing an Exception" in {
    withIterator() { src ⇒
      src.take(5)
        .map(throwException)
        .runWith(TestSink.probe[Seq[Int]])
        .request(Integer.MAX_VALUE)
        .expectError()
    }
  }

  it should "fail the resulting future when throwing an Exception" in {
    withIterator() { src ⇒
      src.take(5).mapAsync(1)(throwException).runForeach(_ ⇒ ()).toTry should be a 'failure
    }
  }
}

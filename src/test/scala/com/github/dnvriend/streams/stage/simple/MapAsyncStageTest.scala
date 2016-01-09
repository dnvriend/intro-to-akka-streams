package com.github.dnvriend.streams.stage.simple

import akka.stream.testkit.scaladsl.TestSink
import com.github.dnvriend.streams.TestSpec

import scala.concurrent.Future

class MapAsyncStageTest extends TestSpec {

  /**
    * Transform this stream by applying the given function to each of the elements
    * as they pass through this processing step.
    *
    * - Emits when: the mapping function returns an element
    * - Backpressures when: downstream backpressures
    * - Completes when: upstream completes
    * - Cancels when: downstream cancels
    */

  it should "transform the stream by applying the function to each element" in {
    withIterator() { src ⇒
      src.take(3)
        .mapAsync(1)(e => Future.successful(e * 2))
        .runWith(TestSink.probe[Int])
        .request(Integer.MAX_VALUE)
        .expectNext(0, 2, 4)
        .expectComplete()
    }
  }

  it should "emit an Error when the Future completes with a failure" in {
    withIterator() { src =>
      src.take(3)
      .mapAsync(1)(_ => Future.failed(new RuntimeException("")))
      .runWith(TestSink.probe[Int])
      .request(Int.MaxValue)
      .expectError()
    }
  }
}

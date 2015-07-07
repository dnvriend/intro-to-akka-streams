package com.github.dnvriend.streams

import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Supervision.resumingDecider
import akka.stream.scaladsl._
import akka.stream.testkit.scaladsl._

import scala.concurrent.Future
import scala.util.{Success, Failure, Try}

class FlowErrorTest extends TestSpec {

  "Error stream" should "" in {
  }

//  it should "stop the stream" in {
//    Source(Future[String](throw new RuntimeException("Test")))
//      .withAttributes(supervisionStrategy(resumingDecider))
//      .map { x => println(x); x }
//      .runWith(TestSink.probe[String])
//      .request(1)
//      .expectError()
//  }

  it should "resume with no result for the failed future" in {
    val t = new RuntimeException("Test")
    Source(List(1, 2, 3))
      .log("before")
      .mapAsync(3){ x =>
      Future {
        if (x == 2) throw t else x
      }
    }
      .withAttributes(supervisionStrategy(resumingDecider))
      .log("after")
      .runWith(TestSink.probe[Int])
      .request(4)
      /* it will drop the failed future so no marble there
        (1) (2) (3)
        [ mapAync ]
        (1)     (3)
      */
      .expectNext(1)
      .expectNext(3)
      .expectComplete()
  }

  it should "resume and return results for all values" in {
    val t = new RuntimeException("Test")
    Source(List(1, 2, 3))
      .log("before")
      .mapAsync(1){ x =>
        Future {
          if (x == 2) throw t else Try(x)
        } .recover { case t: Throwable =>
          Failure(t)
        }
      }
//      .withAttributes(supervisionStrategy(resumingDecider))
      .log("after")
      .runWith(TestSink.probe[Try[Int]])
      .request(4)
      /* The future will return a Future[Try[T]], which can be recovered
         so all marbles are there
        (1)     (2)    (3)
        [      mapAync     ]
        (S(1)) (F(t)) (S(3))
      */
      .expectNext(Success(1))
      .expectNext(Failure(t))
      .expectNext(Success(3))
      .expectComplete()
  }
}

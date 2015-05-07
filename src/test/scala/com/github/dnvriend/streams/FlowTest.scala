package com.github.dnvriend.streams

import akka.event.Logging
import akka.stream.{Shape, OperationAttributes}
import akka.stream.scaladsl._
import akka.stream.testkit.scaladsl._

import scala.concurrent.Future

class FlowTest extends TestSpec {

  /**               / -- f2 --\
   * (in) --- (bcast)         (merge) -- f3 -- (out)
   *               | -- f4 -- /
   */

  val ignoreSink: Sink[Int, Unit] = Sink.ignore
  val resultSink: Sink[Int, Future[Int]] = Sink.head[Int]
  val in: Source[Int, Unit] = Source(1 to 1)

  def debug(name: String, in: Any, out: Any): Unit = log.info(s"[$name] ($in) -> ($out)")

  "SimpleFlow" should "receive single scalar number" in {
    val g = FlowGraph.closed(resultSink) { implicit builder: FlowGraph.Builder[Future[Int]] => out =>
      import FlowGraph.Implicits._
      val bcast = builder.add(Broadcast[Int](2))
      val merge = builder.add(Merge[Int](2))

      val f1 = Flow[Int]
        .map { in =>
          val out = in + 10
          debug("f1", in, out)
          out
        }
      val f2 = Flow[Int]
        .map { in =>
        val out = in + 10
        debug("f2", in, out)
        out
      }
      val f3 = Flow[Int]
        .map { in =>
        val out = in + 10
        debug("f3", in, out)
        out
      }
      val f4 = Flow[Int]
        .map { in =>
        val out = in + 10
        debug("f4", in, out)
        out
      }

      in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
      bcast ~> f4 ~> merge
    }

    g.run().futureValue shouldBe 31
  }

  it should "consume all messages" in {
    log.info("Consuming all messages")
    val in: Source[Int, Unit] = Source(1 to 10)

    val g = FlowGraph.closed() { implicit builder =>
      import FlowGraph.Implicits._
      val bcast = builder.add(Broadcast[Int](2))
      val merge = builder.add(Merge[Int](2))

      val f1 = Flow[Int]
        .map { in =>
        val out = in + 10
        debug("f1", in, out)
        out
      }
      val f2 = Flow[Int]
        .map { in =>
        val out = in + 10
        debug("f2", in, out)
        out
      }
      val f3 = Flow[Int]
        .map { in =>
        val out = in + 10
        debug("f3", in, out)
        out
      }
      val f4 = Flow[Int]
        .map { in =>
        val out = in + 10
        debug("f4", in, out)
        out
      }

      in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> ignoreSink
      bcast ~> f4 ~> merge
    }
    g.run()
  }
}

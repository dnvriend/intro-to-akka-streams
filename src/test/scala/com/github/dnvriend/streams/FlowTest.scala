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

import akka.stream.scaladsl._

import scala.concurrent.Future

class FlowTest extends TestSpec {

  /**
   *              / -- f2 --\
   * (in) --- (bcast)         (merge) -- f3 -- (out)
   *               | -- f4 -- /
   */

  val ignoreSink: Sink[Int, Future[Unit]] = Sink.ignore
  val resultSink: Sink[Int, Future[Int]] = Sink.head[Int]
  val in: Source[Int, Unit] = Source(1 to 1)

  "SimpleFlow" should "receive single scalar number" in {
    val g = FlowGraph.closed(resultSink) { implicit builder: FlowGraph.Builder[Future[Int]] ⇒
      out ⇒
        import FlowGraph.Implicits._
        val bcast = builder.add(Broadcast[Int](2))
        val merge = builder.add(Merge[Int](2))

        val f1 = Flow[Int].map(_ + 10).log("f1")
        val f2 = Flow[Int].map(_ + 20).log("f2")
        val f3 = Flow[Int].map(_ + 30).log("f3")
        val f4 = Flow[Int].map(_ + 40).log("f4")

        in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
        bcast ~> f4 ~> merge
    }

    g.run().futureValue shouldBe 61
  }

  it should "consume all messages" in {
    log.info("Consuming all messages")
    val in: Source[Int, Unit] = Source(1 to 10)

    val g = FlowGraph.closed() { implicit builder ⇒
      import FlowGraph.Implicits._
      val bcast = builder.add(Broadcast[Int](2))
      val merge = builder.add(Merge[Int](2))

      val f1 = Flow[Int].map(_ + 10).log("f1")
      val f2 = Flow[Int].map(_ + 20).log("f2")
      val f3 = Flow[Int].map(_ + 30).log("f3")
      val f4 = Flow[Int].map(_ + 40).log("f4")

      in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> ignoreSink
      bcast ~> f4 ~> merge
    }
    g.run()
  }
}

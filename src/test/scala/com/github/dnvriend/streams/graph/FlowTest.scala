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

package com.github.dnvriend.streams.graph

import akka.stream.scaladsl._
import com.github.dnvriend.streams.TestSpec

import scala.concurrent.Future

class FlowTest extends TestSpec {

  /**
   *                / -- f2 --\
   * (in) - f1 - -- (bcast)   (zip) -- f4 -- (out)
   *               | -- f3 -- /
   */

  val resultSink: Sink[Int, Future[Int]] = Sink.head[Int]
  val in: Source[Int, Unit] = Source.single(1)

  "SimpleFlow" should "receive single scalar number" in {
    val g = FlowGraph.closed(resultSink) { implicit b ⇒
      out ⇒
        import FlowGraph.Implicits._
        val bcast = b.add(Broadcast[Int](2))
        val zip = b.add(Zip[Int, Int])

        val f1 = Flow[Int].map(_ + 10)
        val f2 = Flow[Int].map(_ + 20)
        val f3 = Flow[Int].map(_ + 30)
        val f4 = Flow[(Int, Int)].map { case (x, y) ⇒ x + y }

        in ~> f1 ~> bcast ~> f2 ~> zip.in0 // 11 will be broadcasted, 11 + 20 = 31 in f2,
        bcast ~> f3 ~> zip.in1 // 11 + 30 = 41 in f3
        zip.out ~> f4 ~> out // 41 + 31 will be added in f4, which will be 72
    }
    g.run().futureValue shouldBe 72
  }
}

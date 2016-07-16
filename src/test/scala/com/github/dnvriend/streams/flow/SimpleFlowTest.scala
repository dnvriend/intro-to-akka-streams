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

package com.github.dnvriend.streams.flow

import akka.stream.{ OverflowStrategy, SourceShape }
import akka.stream.scaladsl.{ Concat, Framing, GraphDSL, Merge, RunnableGraph, Sink, Source }
import akka.util.ByteString
import com.github.dnvriend.streams.TestSpec
import com.github.dnvriend.streams.flow.SimpleFlowTest.StarWars

import scala.collection.immutable.{ Iterable, Seq }
import scala.concurrent.Future

object SimpleFlowTest {

  final case class StarWars(first: String, last: String)

}

class SimpleFlowTest extends TestSpec {

  it should "mapAsync with odd number of parallelism" in {
    Source(1 to 3).mapAsync(5)(i ⇒ Future(i * 2))
      .runWith(Sink.seq).futureValue shouldBe Seq(2, 4, 6)
  }

  it should "zip with an index" in {
    Source(Seq("a", "b")).statefulMapConcat { () ⇒
      var index = 0L
      def next: Long = {
        index += 1
        index
      }
      (string) ⇒ Iterable((string, next))
    }.take(10).runWith(Sink.seq).futureValue shouldBe Seq(("a", 1), ("b", 2))

    Source(List("a", "b", "c"))
      .zip(Source.fromIterator(() ⇒ Iterator from 1))
      .runWith(Sink.seq).futureValue shouldBe Seq(("a", 1), ("b", 2), ("c", 3))
  }

  it should "emit only odd numbers" in {
    Source.fromIterator(() ⇒ Iterator from 0).statefulMapConcat { () ⇒
      var index = 1L
      def next: Long = {
        index += 1L
        if (index % 2 != 0) index else {
          next
        }
      }
      (string) ⇒ Iterable((string, next))
    }.take(10).runForeach(println)
  }

  it should "create tuples" in {
    Source(List(List("a", "b"), List("c", "d")))
      .flatMapConcat { xs ⇒
        Source(xs).take(1).zip(Source(xs).drop(1))
      }.runWith(Sink.seq).futureValue shouldBe Seq(("a", "b"), ("c", "d"))
  }

  it should "parse some csv from the classpath" in withByteStringSource("csv/starwars.csv") { src ⇒
    src.via(Framing.delimiter(ByteString("\n"), Integer.MAX_VALUE))
      .map(_.utf8String)
      .drop(1)
      .map(_.split(",").toList)
      .flatMapConcat { xs ⇒
        Source(xs).take(1).zip(Source(xs).drop(1))
      }.map(StarWars.tupled)
      .runWith(Sink.seq).futureValue shouldBe Seq(
        StarWars("darth", "vader"),
        StarWars("leia", "organa"),
        StarWars("luke", "skywalker"),
        StarWars("han", "solo"),
        StarWars("boba", "fett"),
        StarWars("obi-wan", "kenobi"),
        StarWars("darth", "maul"),
        StarWars("darth", "sidious"),
        StarWars("padme", "amidala"),
        StarWars("lando", "calrissian"),
        StarWars("mace", "windu")
      )
  }

  it should "concat" in {
    Source(List(1, 2)).concat(Source(List(3, 4)))
      .runWith(Sink.seq).futureValue shouldBe Seq(1, 2, 3, 4)
  }

  it should "merge" in {
    Source.fromGraph(GraphDSL.create() { implicit b ⇒
      import GraphDSL.Implicits._
      val merge = b.add(Concat[Int](2))
      Source.single(1) ~> merge
      Source.repeat(5) ~> merge
      SourceShape(merge.out)
    }).take(4).runWith(Sink.seq).futureValue shouldBe Seq(1, 5, 5, 5)

    Source.single(1).concat(Source.repeat(5))
      .take(4).runWith(Sink.seq).futureValue shouldBe Seq(1, 5, 5, 5)
  }

  it should "unfold" in {
    import scala.concurrent.duration._
    Source.tick(0.seconds, 500.millis, 0).flatMapConcat { _ ⇒
      Source.unfold(0) { (e) ⇒
        val next = e + 1
        if (next > 3) None else Some((next, next))
      }
    }.take(6).runWith(Sink.seq).futureValue shouldBe Seq(1, 2, 3, 1, 2, 3)
  }
}

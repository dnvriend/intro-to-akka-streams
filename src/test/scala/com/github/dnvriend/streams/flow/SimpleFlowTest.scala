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

import akka.stream.scaladsl.{ Framing, Sink, Source }
import akka.util.ByteString
import com.github.dnvriend.streams.TestSpec
import com.github.dnvriend.streams.flow.SimpleFlowTest.StarWars

import scala.collection.immutable.{ Iterable, Seq }

object SimpleFlowTest {
  final case class StarWars(first: String, last: String)
}

class SimpleFlowTest extends TestSpec {

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
}

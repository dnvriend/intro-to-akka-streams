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

package com.github.dnvriend.streams.customstage

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.stage.{ GraphStage, GraphStageLogic, OutHandler }
import akka.stream.testkit.TestSubscriber
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ Attributes, Materializer, Outlet, SourceShape }
import com.github.dnvriend.streams.TestSpec

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

class CustomStreamProcessingTest extends TestSpec {

  "Custom Number Source" should "" in {
    CustomNumbersSource.withTestProbe() { tp ⇒
      tp.request(1)
      tp.expectNext(1)
      tp.request(1)
      tp.expectNext(2)
      tp.cancel()
      tp.expectNoMsg(100.millis)
    }

    CustomNumbersSource.withTestProbe() { tp ⇒
      tp.request(2)
      tp.expectNext(1)
      tp.expectNext(2)
      tp.cancel()
      tp.expectNoMsg(100.millis)
    }

    CustomNumbersSource.withTestProbe() { tp ⇒
      tp.request(3)
      tp.expectNext(1)
      tp.expectNext(2)
      tp.expectNext(3)
      tp.cancel()
      tp.expectNoMsg(100.millis)
    }

    CustomNumbersSource.withTestProbe() { tp ⇒
      tp.cancel()
      tp.expectNoMsg(100.millis)
    }
  }
}

class CustomNumbersSource extends GraphStage[SourceShape[Int]] {
  val out: Outlet[Int] = Outlet("NumbersSource")
  override val shape: SourceShape[Int] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      private var counter = 1

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          push(out, counter)
          counter += 1
        }
      })
    }
}
object CustomNumbersSource {
  def apply(): Source[Int, NotUsed] = Source.fromGraph(new CustomNumbersSource)

  def withTestProbe(within: FiniteDuration = 10.seconds)(f: TestSubscriber.Probe[Int] ⇒ Unit)(implicit system: ActorSystem, mat: Materializer): Unit = {
    val probe = apply().runWith(TestSink.probe[Int])
    f(probe.within(within)(probe))
  }
}

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

package com.github.dnvriend.streams.sink

import akka.Done
import akka.actor.Actor.Receive
import akka.actor.{ ActorRef, Props }
import akka.event.LoggingReceive
import akka.stream.actor.ActorSubscriberMessage.{ OnComplete, OnError, OnNext }
import akka.stream.actor.{ ActorSubscriber, OneByOneRequestStrategy, RequestStrategy }
import akka.stream.scaladsl.{ Sink, Source }
import akka.stream.testkit.TestPublisher
import akka.stream.testkit.scaladsl.TestSource
import akka.testkit.TestProbe
import com.github.dnvriend.streams.TestSpec
import com.github.dnvriend.streams.sink.ActorSubscriberTest.TestActorSubscriber

import scala.concurrent.Future
import scala.reflect.ClassTag

object ActorSubscriberTest {
  final val OnNextMessage = "onNext"
  final val OnCompleteMessage = "onComplete"
  final val OnErrorMessage = "onError"

  class TestActorSubscriber(ref: ActorRef) extends ActorSubscriber {
    override protected val requestStrategy: RequestStrategy = OneByOneRequestStrategy
    override def receive: Receive = LoggingReceive {
      case OnNext(msg)    ⇒ ref ! OnNextMessage
      case OnComplete     ⇒ ref ! OnCompleteMessage
      case OnError(cause) ⇒ ref ! OnErrorMessage
    }
  }
}

//class ActorSubscriberTest extends TestSpec {
//  def withForwarder(xs: Int*)(f: TestProbe ⇒ Unit): Unit = {
//    val tp = TestProbe()
//    val ref = new TestActorSubscriber(tp.ref)
//    Source(xs.toList).to(Sink.actorSubscriber(Props())).mapMaterializedValue(_ ⇒ Future.successful[Done]).run()
//    try f(tp) finally killActors(ref)
//  }
//
//}

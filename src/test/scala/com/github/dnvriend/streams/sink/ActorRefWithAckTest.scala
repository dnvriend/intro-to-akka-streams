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

import akka.actor.{ Actor, ActorRef, Props }
import akka.stream.scaladsl.{ Sink, Source }
import akka.stream.testkit.TestPublisher
import akka.stream.testkit.scaladsl.TestSource
import akka.testkit.TestProbe
import com.github.dnvriend.streams.TestSpec
import scala.concurrent.duration._

import scala.reflect.ClassTag

// see: https://github.com/akka/akka/blob/4acc1cca6a27be0ff80f801de3640f91343dce94/akka-stream-tests/src/test/scala/akka/stream/scaladsl/ActorRefBackpressureSinkSpec.scala
object ActorRefWithAckTest {
  final val InitMessage = "start"
  final val CompleteMessage = "done"
  final val AckMessage = "ack"

  class Forwarder(ref: ActorRef) extends Actor {
    def receive = {
      case msg @ `InitMessage` ⇒
        sender() ! AckMessage
        ref forward msg
      case msg @ `CompleteMessage` ⇒
        ref forward msg
      case msg ⇒
        sender() ! AckMessage
        ref forward msg
    }
  }
}

class ActorRefWithAckTest extends TestSpec {
  import ActorRefWithAckTest._
  def createActor[A: ClassTag](testProbeRef: ActorRef): ActorRef =
    system.actorOf(Props(implicitly[ClassTag[A]].runtimeClass, testProbeRef))

  def withForwarder(xs: Int*)(f: TestProbe ⇒ Unit): Unit = {
    val tp = TestProbe()
    val ref = createActor[Forwarder](tp.ref)
    Source(xs.toList).runWith(Sink.actorRefWithAck(ref, InitMessage, AckMessage, CompleteMessage))
    try f(tp) finally killActors(ref)
  }

  def withTestPublisher[A](f: (TestPublisher.Probe[A], TestProbe, ActorRef) ⇒ Unit): Unit = {
    val tp = TestProbe()
    val ref = createActor[Forwarder](tp.ref)
    val pub: TestPublisher.Probe[A] = TestSource.probe[A].to(Sink.actorRefWithAck(ref, InitMessage, AckMessage, CompleteMessage)).run()
    try f(pub, tp, ref) finally killActors(ref)
  }

  it should "send the elements to the ActorRef" in {
    // which means that the forwarder actor that acts as a sink
    // will initially receive an InitMessage
    // next it will receive each `payload` element, here 1, 2 and 3,
    // finally the forwarder will receive the CompletedMessage, stating that
    // the producer completes the stream because there are no more elements (a finite stream)
    withForwarder(1, 2, 3) { tp ⇒
      tp.expectMsg(InitMessage)
      tp.expectMsg(1)
      tp.expectMsg(2)
      tp.expectMsg(3)
      tp.expectMsg(CompleteMessage)
      tp.expectNoMsg(100.millis)
    }
  }

  it should "send the elements to the ActorRef manually 1, 2 and 3" in {
    withTestPublisher[Int] { (pub, tp, _) ⇒
      pub.sendNext(1)
      tp.expectMsg(InitMessage)
      tp.expectMsg(1)

      pub.sendNext(2)
      tp.expectMsg(2)

      pub.sendNext(3)
      tp.expectMsg(3)

      pub.sendComplete()
      tp.expectMsg(CompleteMessage)
      tp.expectNoMsg(100.millis)
    }
  }

  it should "cancel stream when actor terminates" in {
    withTestPublisher[Int] { (pub, tp, ref) ⇒
      pub.sendNext(1)
      tp.expectMsg(InitMessage)
      tp.expectMsg(1)
      killActors(ref)
      pub.expectCancellation()
    }
  }
}

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

import akka.actor.{ ActorLogging, Props }
import akka.stream.{ ActorMaterializerSettings, ActorMaterializer, Supervision }
import akka.stream.actor.ActorPublisherMessage._
import akka.stream.actor.ActorSubscriberMessage.{ OnComplete, OnError, OnNext }
import akka.stream.actor.{ ActorPublisher, ActorSubscriber, MaxInFlightRequestStrategy }
import akka.stream.scaladsl._

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

/**
 * A simple stream publisher that generates numbers every time a `Request` for demand has been received.
 * The life cycle state of the subscription is tracked with the following boolean members:
 * <ul>
 * <li>isActive</li>
 * <li>isCompleted</li>
 * <li>isErrorEmitted</li>
 * </li>isCanceled</li>
 * </ul>
 *
 * see: http://doc.akka.io/api/akka-stream-and-http-experimental/1.0-RC2/#akka.stream.actor.ActorPublisher
 */
class NumberPublisher(delay: Long = 50) extends ActorPublisher[Long] with ActorLogging {
  val stopAt = 25
  var counter: Long = 0L

  override def receive = {
    // a subscriber will send the demand message.
    case Request(demand) if totalDemand > 0 && isActive ⇒
      log.info("[Request]: demand: {}", demand)
      if (isActive && totalDemand > 0 && counter <= stopAt)
        try {
          (1L to totalDemand).foreach { _ ⇒
            log.info("Foreach: {}", counter)
            Thread.sleep(delay) // to slow logging down
            onNext(counter)
            counter += 1
          }
        } catch {
          case t: Throwable ⇒
            // You can terminate the stream with failure by calling `onError`.
            // After that you are not allowed to call `onNext`, `onError` and `onComplete`.
            log.error(t, "")
            onError(t)
            stop()
        }
      else {
        log.info(s"$stopAt reached, stopping")
        onComplete()
      }

    case Cancel ⇒
      log.info("[Cancel]")
      // When the stream subscriber cancels the subscription the ActorPublisher.Cancel message
      // is delivered to this actor. After that subsequent calls to `onNext` will be ignored.
      onComplete()

    case SubscriptionTimeoutExceeded ⇒
      log.info("[SubscriptionTimeoutExceeded]")
      onComplete()
      stop()

    case m ⇒ log.info("[!!!DROPPING!!!]: {}", m)
  }

  def stop(): Unit = {
    // If the actor is stopped the stream will be completed,
    // unless it was not already terminated with failure, completed
    // or canceled.
    context.stop(self)
  }

  override def preStart(): Unit = {
    log.info("Starting")
    super.preStart()
  }

  override def postStop(): Unit = {
    log.info("Stopping")
    super.postStop()
  }
}

/**
 *  A stream subscriber with full control of stream back pressure.
 *  It will receive the following messages from the stream:
 *  <ul>
 *    <li>ActorSubscriberMessage.OnNext</li>
 *    <li>ActorSubscriberMessage.OnComplete</li>
 *    <li>ActorSubscriberMessage.OnError</li>
 *  </ul>
 *
 *  It can also receive other, non-stream messages, in the same way as any actor.
 */
class NumberSubscriber(maxInFlight: Int = 1, f: Long ⇒ Unit) extends ActorSubscriber with ActorLogging {
  var inFlight = 0

  // requestStrategy controls stream back pressure. After each incoming message the ActorSubscriber
  // will automatically invoke the `RequestStrategy.requestDemand` and propagate
  // the returned demand to the stream.
  override protected def requestStrategy =
    // Requests up to the max and also takes the number of messages that have been queued
    // internally or delegated to other actors into account. Concrete subclass must implement
    // the method `inFlightInternally`. It will request elements in minimum batches of the
    // defined `batchSize`.
    new MaxInFlightRequestStrategy(max = maxInFlight) {
      override def inFlightInternally = inFlight
    }

  override def receive = {
    case OnNext(msg: Long) ⇒
      inFlight += 1
      Thread.sleep(100) // do some heavy computing :)
      log.info("[OnNext]: {}, inflight: {}", msg, inFlight)
      f(msg)
      inFlight -= 1

    case OnComplete ⇒
      log.info("[OnComplete]")
      stop()

    case OnError ⇒
      log.info("[OnError]")
      stop()

    case m ⇒ log.info("[!!!DROPPING!!!]: {}", m)
  }
  def stop(): Unit = {
    // If the actor is stopped the stream will be completed,
    // unless it was not already terminated with failure, completed
    // or canceled.
    context.stop(self)
  }

  override def preStart(): Unit = {
    log.info("Starting")
    super.preStart()
  }

  override def postStop(): Unit = {
    log.info("Stopping")
    super.postStop()
  }
}

class AkkaPublisherSubscriberTest extends TestSpec {

  /**
   * A println sink
   */
  val printlnSink = Sink.foreach(println)

  /**
   * The FlowGraph that will be reused; it is a simple broadcast, splitting the flow into 2
   * @param sink
   * @return
   */
  def graph(sink: Sink[Long, Future[Unit]], f: Long ⇒ Unit) = FlowGraph.closed(sink) { implicit b ⇒
    sink ⇒
      import FlowGraph.Implicits._
      val src = Source.actorPublisher(Props(new NumberPublisher()))
      val numberSink = Sink.actorSubscriber(Props(new NumberSubscriber(1, f)))
      val bcast = b.add(Broadcast[Long](2))

      src ~> bcast ~> numberSink
      bcast ~> sink
  }

  "NumberProducer" should "count some time" in {
    // the default demand is 4, and will not ask for more
    Source.actorPublisher(Props(new NumberPublisher))
      .runForeach(println)
      .futureValue
  }

  it should "use a subscriber to supply backpressure" in {
    Await.ready(graph(printlnSink, x ⇒ ()).run(), 1.minute)
  }

  it should "throws an exception when count == 10, println continues" in {
    // note, the actor crashes and will be stopped, but the println sink will continue
    Await.ready(graph(printlnSink, x ⇒ if (x == 10) throw new RuntimeException("10 reached")).run(), 1.minute)
  }

  // should use actor supervision for this.. TBC
  ignore should "throw an exception but the actor will recover, the message will be dropped though" in {
    val decider: Supervision.Decider = {
      case _ ⇒ Supervision.Restart
    }
    implicit val mat = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))
    Await.ready(graph(printlnSink, x ⇒ if (x == 10) throw new RuntimeException("10 reached")).run(), 1.minute)
  }
}

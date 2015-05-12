package com.github.dnvriend.streams

import java.util.UUID

import akka.stream.scaladsl._
import akka.stream.testkit.scaladsl._
import scala.collection.immutable
import scala.concurrent.{Promise, Future}

class AkkaStreamsTest extends TestSpec {
  /**
   * The Source, it is a generator for 100 input customers with random first and random last name
   */
  lazy val inputCustomersSource: Source[InputCustomer, Unit] = Source((1 to 100).map(_ => InputCustomer.random()))

  /**
   * The flow, it is a transformer from InputCustomer to OutputCustomer
   */
  lazy val normalizeFlow = Flow[InputCustomer].mapConcat { (inputCustomer: InputCustomer) =>
    inputCustomer.name.split(" ").toList match {
      case firstName :: lastName :: Nil => immutable.Seq(OutputCustomer(firstName, lastName))
      case _ => immutable.Seq[OutputCustomer]()
    }
  }

  /**
   * The sink: it logs all OutputCustomers using the logger
   */
  lazy val writeCustomersSink = Sink.foreach[OutputCustomer] { (outputCustomer: OutputCustomer) =>
    log.info("Customer: {}", outputCustomer)
  }

  "The Akka Stream Chain" should "execute normally" in {
    val chain: Future[Unit] = inputCustomersSource.via(normalizeFlow).runWith(writeCustomersSink)
    chain.toTry should be a 'success
  }

  it should "process 100 customers" in {
    var counter = 0
    val counterSink = Sink.foreach[OutputCustomer] { _ =>
      counter += 1
    }
    inputCustomersSource.via(normalizeFlow).runWith(counterSink).toTry should be a 'success
    counter shouldBe 100
  }

  it should "transform a customer" in {
    inputCustomersSource
      .via(normalizeFlow)
      .runWith(TestSink.probe[OutputCustomer])
      .request(1)
      .expectNext() match {
        case OutputCustomer(_, _) =>
        case u => fail("Unexpected: " + u)
      }
  }

  // Testing Streams
  // see: http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-RC1/scala/stream-testkit.html
  "Probe Sink" should "be testable" in {
    // Using probe as a Sink allows manual control over demand and assertions over elements coming downstream.
    // Streams testkit provides a sink that materializes to a TestSubscriber.Probe.
    Source(1 to 4)
      .filter(_ % 2 == 0)
      .map(_ * 2)
      .runWith(TestSink.probe[Int])
      .request(2)
      .expectNext(4, 8)
      .expectComplete()
  }

  "Probe Source" should "be testable" in {
    // A source that materializes to TestPublisher.Probe can be used for asserting demand or controlling when stream
    // is completed or ended with an error.
    TestSource.probe[Int]
      .toMat(Sink.cancelled)(Keep.left)
      .run()
      .expectCancellation()
  }

  "Source" should "be created from Range" in {
    Source(1 to 2)
      .map(identity)
      .runWith(TestSink.probe[Int])
      .request(2)
      .expectNext(1, 2)
      .expectComplete()
  }

  it should "be created from a List" in {
    Source(List(1, 2))
      .runWith(TestSink.probe[Int])
      .request(2)
      .expectNext(1, 2)
      .expectComplete()
  }

  it should "be created from a Vector" in {
    Source(Vector(1, 2))
      .runWith(TestSink.probe[Int])
      .request(2)
      .expectNext(1, 2)
      .expectComplete()
  }

  val id1 = UUID.randomUUID().toString
  val id2 = UUID.randomUUID().toString

  it should "process orders" in {
    def name: Future[String] = Promise.successful("Dennis").future
    def address: Future[String] = Promise.successful("Somewhere").future

    Source(List(Order(id1, None, None), Order(id2, None, None)))
      .mapAsync(1) { order => name.map(name => order.copy(name = Option(name))) }
      .mapAsync(1) { order => address.map(address => order.copy(address = Option(address))) }
      .runWith(TestSink.probe[Order])
      .request(3)
      .expectNext(Order(id1, Option("Dennis"), Option("Somewhere")))
      .expectNext(Order(id2, Option("Dennis"), Option("Somewhere")))
      .expectComplete()
  }

  val nameFlow = Flow[Order].map { order => order.copy(name = Option("Dennis")) }
  val addressFlow = Flow[Order].map { order => order.copy(address = Option("Somewhere")) }
  val orderSource = Source(List(Order(id1, None, None), Order(id2, None, None)))
  val testSink = TestSink.probe[Order]
  val bcast = Broadcast[Order](2)
  val merge = Merge[Order](2)

  it should "process orders with flows" in {
    orderSource
      .via(nameFlow)
      .via(addressFlow)
      .runWith(testSink)
      .request(3)
      .expectNext(Order(id1, Option("Dennis"), Option("Somewhere")))
      .expectNext(Order(id2, Option("Dennis"), Option("Somewhere")))
      .expectComplete()
  }
}


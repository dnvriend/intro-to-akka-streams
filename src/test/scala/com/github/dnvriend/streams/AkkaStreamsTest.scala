package com.github.dnvriend.streams

import akka.stream.scaladsl._
import scala.collection.immutable
import scala.concurrent.Future

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
}

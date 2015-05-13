package com.github.dnvriend.streams

import akka.stream.scaladsl._
import akka.util.ByteString
import io.scalac.amqp.{Message, Routed}
import slick.backend.DatabasePublisher
import slick.driver.PostgresDriver.api._
import spray.json._

class SlickStreamTest extends TestSpec {
  "slick" should "read all orders but output only the name to sysout" in {
    // from a Slick result
    val orderNameQuery = for (o <- orders) yield o.name
    val orderNameActions = orderNameQuery.result
    val orderNameProducer: DatabasePublisher[String] = db.stream(orderNameActions)
    val orderNameSource: Source[String, Unit] = Source(orderNameProducer)
    orderNameSource
      .runForeach(println)
      .toTry should be a 'success
  }

  it should "read all orders and sysout them" in {
    // from a Slick result
    val allOrdersAction = orders.result
    val allOrdersProducer: DatabasePublisher[Order] = db.stream(allOrdersAction)
    val allOrdersSource: Source[Order, Unit] = Source(allOrdersProducer)
    allOrdersSource
      .runForeach(println)
      .toTry should be a 'success
  }

  it should "place on rabbitmq" in {
    // flows
    val mapToJsonFlow = Flow[Order].map { order => order.toJson.compactPrint }
    val mapToRoutedFlow = Flow[String].map { orderJson => Routed(routingKey = "", Message(body = ByteString(orderJson))) }
    val debugFlow = Flow[Order].map { order => println("sending to rabbitmq: " + order); order }
    val delayFlow = Flow[Order].map { order => Thread.sleep(10); order }

    // source
    val allOrdersAction = orders.result
    val allOrdersProducer: DatabasePublisher[Order] = db.stream(allOrdersAction)
    val sourceOrders: Source[Order, Unit] = Source(allOrdersProducer)

    // sinks
    val sinkProcessedOrders: Sink[Routed, Unit] = Sink(connection.publish(RabbitRegistry.outboundOrderExchange.name))
    val sinkCount = Sink.fold[Int, Order](0) { case (c, _) => c + 1 }

    val jsonRoutedFlow: Flow[Order, Routed, Unit] =
      mapToJsonFlow
        .via(mapToRoutedFlow)

    val sinkRabbit: Sink[Order, Unit] =
      jsonRoutedFlow
        .toMat(sinkProcessedOrders)(Keep.right)

    // add the components to the graph builder. They can then be used in the graph
    val g = FlowGraph.closed(sourceOrders, sinkRabbit, sinkCount)((_, _, _)) { implicit builder =>
      (src, sinkRabbit, sinkCount) =>
        import FlowGraph.Implicits._
        val broadcast = builder.add(Broadcast[Order](3))

        val printlnSink = Sink.foreach[Order](x => log.info("Sending to Rabbit: {}", x))

        src ~> broadcast.in
        broadcast.out(0) ~> sinkRabbit
        broadcast.out(1) ~> sinkCount
        broadcast.out(2) ~> printlnSink
    } // end of graph

    val (_, _, future) = g.run()
    println("Processed: " + future.futureValue + " messages")
  }
}

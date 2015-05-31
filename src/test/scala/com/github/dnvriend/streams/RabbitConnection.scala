package com.github.dnvriend.streams

import akka.actor.{Extension, ExtendedActorSystem, ExtensionIdProvider, ExtensionId}
import akka.event.{Logging, LoggingAdapter}
import akka.stream.scaladsl.Source
import akka.stream.{ActorFlowMaterializer, FlowMaterializer}
import io.scalac.amqp.{Queue, Direct, Exchange, Connection}

import scala.concurrent.Future

object RabbitConnection extends ExtensionId[RabbitConnectionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): RabbitConnectionImpl = new RabbitConnectionImpl()(system)

  override def lookup(): ExtensionId[_ <: Extension] = RabbitConnection
}

class RabbitConnectionImpl()(implicit val system: ExtendedActorSystem) extends Extension {
  implicit val flowMaterializer: FlowMaterializer = ActorFlowMaterializer()
  implicit val log: LoggingAdapter = Logging(system, this.getClass)
  implicit val ec = system.dispatcher

  lazy val connection = Connection()

  def init: Future[Int] =
    Source(1 to 1)
      // declare and bind to the orders queue
      .mapAsync(1)(_ => connection.exchangeDeclare(RabbitRegistry.outboundOrderExchange))
      .mapAsync(1)(_ => connection.queueDeclare(RabbitRegistry.inboundOrdersQueue))
      .mapAsync(1)(_ => connection.queueBind(RabbitRegistry.inboundOrdersQueue.name, RabbitRegistry.outboundOrderExchange.name, ""))
      .runFold(0) { case (c, _) => c + 1}
}

object RabbitRegistry {
  val outboundOrderExchange = Exchange(name = "orders.outbound.exchange", `type` = Direct, durable = true)

  val inboundOrdersQueue = Queue(name = "orders.inbound.queue", durable = true)
}
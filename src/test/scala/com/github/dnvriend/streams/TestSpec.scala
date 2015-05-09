package com.github.dnvriend.streams

import akka.actor._
import akka.event.{Logging, LoggingAdapter}
import akka.stream.{FlowMaterializer, ActorFlowMaterializer}
import akka.stream.scaladsl._
import io.scalac.amqp.{Connection, Direct, Exchange, Queue}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcBackend
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class Order(orderId: String, name: Option[String], address: Option[String])

trait TestSpec extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("TestSystem")
  implicit val pc: PatienceConfig = PatienceConfig(timeout = 50.seconds)
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val flowMaterializer: FlowMaterializer = ActorFlowMaterializer()
  implicit val log: LoggingAdapter = Logging(system, this.getClass)
  implicit val orderJsonFormat = jsonFormat3(Order)
  val dbDomain = DatabaseDomain(system)
  val rabbit = RabbitConnection(system)
  val orders = dbDomain.orders
  val db = dbDomain.db
  val connection = rabbit.connection

  implicit class FutureToTry[T](f: Future[T]) {
    def toTry: Try[T] = Try(f.futureValue)
  }

  rabbit.init.flatMap { _ =>
    dbDomain.init
  }.futureValue


  override protected def afterAll(): Unit = {
    system.shutdown()
    system.awaitTermination()
  }
}

class Orders(tag: Tag) extends Table[Order](tag, "orders") {
  def orderId = column[String]("order_id", O.PrimaryKey)
  def name = column[String]("name", O.Nullable)
  def address = column[String]("address", O.Nullable)
  def * = (orderId, name.?, address.?) <> (Order.tupled, Order.unapply)
}

object DatabaseDomain extends ExtensionId[DatabaseDomainImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): DatabaseDomainImpl = new DatabaseDomainImpl()(system)

  override def lookup(): ExtensionId[_ <: Extension] = DatabaseDomain
}

class DatabaseDomainImpl()(implicit val system: ExtendedActorSystem) extends JdbcBackend with Extension {
  implicit val flowMaterializer: FlowMaterializer = ActorFlowMaterializer()
  implicit val log: LoggingAdapter = Logging(system, this.getClass)
  implicit val ec = system.dispatcher

  lazy val db = Database.forConfig("mydb")

  lazy val orders = TableQuery[Orders]

  private val schema = orders.schema

  def init: Future[Int] =
    createSchema.flatMap(_ => fillDb)

  def createSchema: Future[Unit] =
    db.run(schema.create)
      .recoverWith { case t: Throwable =>
      log.info("drop-create")
      db.run(DBIO.seq(schema.drop, schema.create))
    }

  def fillDb: Future[Int] =
    Source(1 to 100) // 100000
      .map(id => orders += Order(s"$id", Option(s"name-$id"), Option(s"address-$id")))
      .mapAsync(10) (db.run)
      .runFold(0) { case (c, _) => c + 1 }
}

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
    Source(1 to 3)
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

trait FlowFactory {
  // processing pipelines here; integrate over remote and/or local producers/consumers

  // the flows can communicate with local/remote services and/or actors
}

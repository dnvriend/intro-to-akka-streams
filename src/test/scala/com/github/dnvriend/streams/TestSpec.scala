package com.github.dnvriend.streams

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcBackend

import io.scalac.amqp.{Connection, Direct, Exchange, Queue}
import spray.json.DefaultJsonProtocol

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

case class Order(orderId: String, name: Option[String], address: Option[String])

trait TestSpec extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll with RabbitConnection with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("TestSystem")
  implicit val pc: PatienceConfig = PatienceConfig(timeout = 50.seconds)
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val flowMaterializer = ActorFlowMaterializer()
  implicit val log: LoggingAdapter = Logging(system, this.getClass)

  implicit val orderJsonFormat = jsonFormat3(Order)

  implicit class FutureToTry[T](f: Future[T]) {
    def toTry: Try[T] = Try(f.futureValue)
  }

  val db = DatabaseDomain.db
    initRabbitMQ().flatMap { _ =>
      DatabaseDomain.init
    }.futureValue

  println("inserting items in DB")

  val start = System.currentTimeMillis()
  val r = Source(1 to 100) // 100000
    .map(id => DatabaseDomain.orders += Order(s"$id", Option(s"name-$id"), Option(s"address-$id")))
    .mapAsync(10) { cmd => db.run(cmd) }
    .runWith(Sink.foreach(_ => ()))

  println("Done: " + r.futureValue + " took: " + (System.currentTimeMillis() - start))

  val orders = DatabaseDomain.orders

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

object DatabaseDomain extends JdbcBackend {

  import scala.concurrent.ExecutionContext.Implicits.global

  val db = Database.forConfig("mydb")

  val orders = TableQuery[Orders]

  val schema = orders.schema

  def init()(implicit ec: ExecutionContext) =
    db.run(schema.create)
      .recoverWith { case t: Throwable =>
      println("drop-create")
      db.run(DBIO.seq(schema.drop, schema.create))
    }
}

trait RabbitConnection {
  val connection = Connection()

  def initRabbitMQ()(implicit ec: ExecutionContext): Future[Unit] =
    Future.sequence(List(
      // declare and bind to the orders queue
      connection.exchangeDeclare(RabbitRegistry.outboundOrderExchange),
      connection.queueDeclare(RabbitRegistry.inboundOrdersQueue)
    ))
    .flatMap { _ =>
      connection.queueBind(RabbitRegistry.inboundOrdersQueue.name, RabbitRegistry.outboundOrderExchange.name, "")
    }.map(_ => ())
}

object RabbitRegistry {
  // exchange and queues here

  val outboundOrderExchange = Exchange("orders.outbound.exchange", Direct, durable = true)

  val inboundOrdersQueue = Queue("orders.inbound.queue")
}

trait FlowFactory {
  // processing pipelines here; integrate over remote and/or local producers/consumers

  // the flows can communicate with local/remote services and/or actors
}

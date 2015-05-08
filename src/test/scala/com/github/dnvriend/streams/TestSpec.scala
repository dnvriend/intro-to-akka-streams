package com.github.dnvriend.streams

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorFlowMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcBackend

import io.scalac.amqp.{Direct, Exchange, Queue}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

case class Order(orderId: String, name: Option[String], address: Option[String])

trait TestSpec extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll with DatabaseDomain {
  implicit val system: ActorSystem = ActorSystem("TestSystem")
  implicit val pc: PatienceConfig = PatienceConfig(timeout = 50.seconds)
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val flowMaterializer = ActorFlowMaterializer()
  implicit val log: LoggingAdapter = Logging(system, this.getClass)

  implicit class FutureToTry[T](f: Future[T]) {
    def toTry: Try[T] = Try(f.futureValue)
  }

  override protected def afterAll(): Unit = {
    db.close()
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

trait DatabaseDomain extends JdbcBackend {

  def orders = TableQuery[Orders]

  implicit val db: Database = Database.forConfig("mydb")
  def schema = orders.schema

  def insertOrdersAction = (1 to 100).map(id => orders += Order(s"$id", Option(s"name-$id"), Option(s"address-$id"))).toList

  def databaseActions = List(schema.drop, schema.create) ++ insertOrdersAction

  def initDatabase: Future[Unit] = db.run(DBIO.seq(databaseActions:_*))

  //  db.run(orders.result).foreach(println)
  //  val q = for(o <- orders) yield o.name
  //  db.run(q.result).foreach(println)
}

object RabbitRegistry {
  // exchange and queues here
}

trait FlowFactory {
  // processing pipelines here; integrate over remote and/or local producers/consumers

  // the flows can communicate with local/remote services and/or actors
}

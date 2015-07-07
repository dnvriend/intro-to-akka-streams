package com.github.dnvriend.streams

import akka.actor._
import akka.event.{Logging, LoggingAdapter}
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, Materializer}
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcBackend

import scala.concurrent.Future

case class Order(orderId: String, name: Option[String], address: Option[String])

class Orders(tag: Tag) extends Table[Order](tag, "orders") {
  def orderId = column[String]("order_id", O.PrimaryKey)
  def name = column[Option[String]]("name")
  def address = column[Option[String]]("address")
  def * = (orderId, name, address) <> (Order.tupled, Order.unapply)
}

object DatabaseDomain extends ExtensionId[DatabaseDomainImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): DatabaseDomainImpl = new DatabaseDomainImpl()(system)

  override def lookup(): ExtensionId[_ <: Extension] = DatabaseDomain
}

class DatabaseDomainImpl()(implicit val system: ExtendedActorSystem) extends JdbcBackend with Extension {
  implicit val mat: Materializer = ActorMaterializer()
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


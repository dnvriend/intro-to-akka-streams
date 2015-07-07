package com.github.dnvriend.streams

import akka.actor._
import akka.event.{Logging, LoggingAdapter}
import akka.stream.{ActorMaterializer, Materializer}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait TestSpec extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val mat: Materializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging(system, this.getClass)
  implicit val orderJsonFormat = jsonFormat3(Order)
  implicit val pc: PatienceConfig = PatienceConfig(timeout = 50.seconds)

  implicit class FutureToTry[T](f: Future[T]) {
    def toTry: Try[T] = Try(f.futureValue)
  }

  override protected def afterAll(): Unit = {
    system.shutdown()
    system.awaitTermination()
  }
}

trait Storage extends ScalaFutures {
  implicit def system: ActorSystem
  implicit def ec: ExecutionContext
  implicit def pc: PatienceConfig
  val dbDomain = DatabaseDomain(system)
//  val mongoDomain = MongoDBDomain(system)
  val rabbit = RabbitConnection(system)
  val orders = dbDomain.orders
  val db = dbDomain.db
  val connection = rabbit.connection

  rabbit.init.flatMap { _ =>
    dbDomain.init
  }.futureValue
}

trait FlowFactory {
  // processing pipelines here; integrate over remote and/or local producers/consumers

  // the flows can communicate with local/remote services and/or actors
}

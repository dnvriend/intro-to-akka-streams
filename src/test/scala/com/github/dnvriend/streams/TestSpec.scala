package com.github.dnvriend.streams

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.stream.ActorFlowMaterializer
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.{Future, ExecutionContext}
import scala.util.Try

trait TestSpec extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll {
  implicit val system: ActorSystem = ActorSystem("TestSystem")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val flowMaterializer = ActorFlowMaterializer()
  val log: LoggingAdapter = Logging(system, this.getClass)

  implicit class FutureToTry[T](f: Future[T]) {
    def toTry: Try[T] = Try(f.futureValue)
  }

  override protected def afterAll(): Unit = {
    system.shutdown()
    system.awaitTermination()
  }
}

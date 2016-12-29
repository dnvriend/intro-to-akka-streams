/*
 * Copyright 2016 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend.streams

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem, PoisonPill }
import akka.event.{ Logging, LoggingAdapter }
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.stream.testkit.TestSubscriber
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.TestProbe
import akka.util.Timeout
import com.github.dnvriend.streams.util.ClasspathResources
import org.scalatest._
import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.BindingKey
import play.api.libs.json.{ Format, Json }
import play.api.test.WsTestClient

import scala.collection.immutable._
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag
import scala.util.Try

object Person {
  implicit val format: Format[Person] = Json.format[Person]
}

final case class Person(firstName: String, age: Int)

class TestSpec extends FlatSpec
    with Matchers
    with GivenWhenThen
    with OptionValues
    with TryValues
    with ScalaFutures
    with WsTestClient
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with Eventually
    with ClasspathResources
    with GuiceOneServerPerSuite {

  def getComponent[A: ClassTag] = app.injector.instanceOf[A]
  def getNamedComponent[A](name: String)(implicit ct: ClassTag[A]): A =
    app.injector.instanceOf[A](BindingKey(ct.runtimeClass.asInstanceOf[Class[A]]).qualifiedWith(name))

  // set the port number of the HTTP server
  override lazy val port: Int = 8081
  implicit val timeout: Timeout = 1.second
  implicit val pc: PatienceConfig = PatienceConfig(timeout = 30.seconds, interval = 300.millis)
  implicit val system: ActorSystem = getComponent[ActorSystem]
  implicit val ec: ExecutionContext = getComponent[ExecutionContext]
  implicit val mat: Materializer = getComponent[Materializer]
  val log: LoggingAdapter = Logging(system, this.getClass)

  // ================================== Supporting Operations ====================================
  def id: String = java.util.UUID.randomUUID().toString

  implicit class FutureToTry[T](f: Future[T]) {
    def toTry: Try[T] = Try(f.futureValue)
  }

  implicit class SourceOps[A](src: Source[A, NotUsed]) {
    def testProbe(f: TestSubscriber.Probe[A] ⇒ Unit): Unit =
      f(src.runWith(TestSink.probe(system)))
  }

  def withIterator[T](start: Int = 0)(f: Source[Int, NotUsed] ⇒ T): T =
    f(Source.fromIterator(() ⇒ Iterator from start))

  def fromCollection[A](xs: Iterable[A])(f: TestSubscriber.Probe[A] ⇒ Unit): Unit =
    f(Source(xs).runWith(TestSink.probe(system)))

  def killActors(refs: ActorRef*): Unit = {
    val tp = TestProbe()
    refs.foreach { ref ⇒
      tp watch ref
      tp.send(ref, PoisonPill)
      tp.expectTerminated(ref)
    }
  }
}
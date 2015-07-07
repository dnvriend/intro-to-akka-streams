package com.github.dnvriend.streams

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer
import akka.stream.scaladsl._

import scala.concurrent.Future

object StreamingClient {
  def doGet(host: String, port: Int, path: String)(implicit system: ActorSystem, mat: Materializer): Future[HttpResponse] = {
    val conn: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] = Http().outgoingConnection(host, port)
    val request = HttpRequest(GET, uri = path)
    Source.single(request).via(conn).runWith(Sink.head[HttpResponse])
  }
}

//package com.github.dnvriend.streams.mongo
//
//import akka.actor._
//import akka.event.{Logging, LoggingAdapter}
//import akka.stream.scaladsl._
//import akka.util.Timeout
//import net.fehmicansaglam.bson.BsonDocument
//import net.fehmicansaglam.tepkin.MongoClient
//
//import scala.concurrent.Future
//import scala.concurrent.duration._
//
//object MongoDBDomain extends ExtensionId[MongoDBDomainImpl] with ExtensionIdProvider {
//  override def createExtension(system: ExtendedActorSystem): MongoDBDomainImpl = new MongoDBDomainImpl()(system)
//
//  override def lookup(): ExtensionId[_ <: Extension] = MongoDBDomain
//}
//
//class MongoDBDomainImpl()(implicit val system: ExtendedActorSystem) extends Extension {
//  implicit val flowMaterializer: FlowMaterializer = ActorFlowMaterializer()
//  implicit val log: LoggingAdapter = Logging(system, this.getClass)
//  import system.dispatcher
//
//  // Connect to a MongoDB node.
//  val mongo = MongoClient("mongodb://boot2docker")
//
//  // Obtain a reference to the "tepkin" database
//  val db = mongo("tepkin")
//
//  // Obtain a reference to the "example" collection in "tepkin" database.
//  val collection = db("example")
//
//  import net.fehmicansaglam.bson.BsonDsl._
//  implicit val timeout: Timeout = 5.seconds
//
//  def insert: Future[Unit] =
//    Source(1 to 1)
//      .map(i => $document("name" := s"fehmi$i"))
//      .mapAsync(1)(collection.insert)
//      .runForeach(println)
//
//  val query: BsonDocument = "name" := "fehmi1"
//
//  def get: Source[List[BsonDocument], ActorRef] = collection.find(query)
//}
//

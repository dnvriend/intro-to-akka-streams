//package com.github.dnvriend.streams.pgextensions
//
//import java.sql.DriverManager
//
//import akka.util.ByteString
//import com.github.dnvriend.streams.TestSpec
//
//class PGExtensionsTest extends TestSpec {
//  val conn = DriverManager.getConnection("jdbc:postgresql://boot2docker:5432/docker","docker", "docker")
//  implicit val pgConnection = PgStream.sqlConnAsPgConnUnsafe(conn)
//  implicit val blockingEc = ExecutionContextForBlockingOps(ec)
//
//  PgStream
//    .getQueryResultAsStream(
//      "select order_id, name, address from orders",
//      options = Map("FORMAT" -> "CSV")
//    )
//    .via(FlowExt.rechunkByteStringBySeparator(ByteString("\n"), maximumChunkBytes = 5 * 1024))
//    .runForeach(bs => println(bs.decodeString("UTF-8")))
//}

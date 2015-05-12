package com.github.dnvriend.streams

// see: https://github.com/abrighton/akka-http-test/blob/master/src/main/scala/akkahttp/test/TestClient.scala
class StreamingClientTest extends TestSpec {

  "StreamingClient" should "Get from google" in {
    val resp = StreamingClient.doGet("www.google.com", 80, "/").futureValue
    println(resp)
  }

}

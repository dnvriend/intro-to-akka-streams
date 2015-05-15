package com.github.dnvriend.streams

import akka.stream.OperationAttributes
import akka.stream.scaladsl._

class CounterTest extends TestSpec {

  /**
   * A single inputBuffer flow
   */
  val single = Flow[Int].withAttributes(OperationAttributes.inputBuffer(initial = 1, max = 1))

  def debug[T] = Flow[T].map { x => println(x); x }

  "Iterator" should "count to ten" in {
    Source(() => Iterator from 0)
      .via(single)
      .take(10)
      .via(debug)
      .runFold(0)((c, _) => c + 1)
      .futureValue shouldBe 10
  }
}

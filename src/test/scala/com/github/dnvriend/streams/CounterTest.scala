package com.github.dnvriend.streams

import akka.stream.Attributes
import akka.stream.scaladsl.{Flow, Source}

class CounterTest extends TestSpec {

  /**
   * A single inputBuffer flow
   */
  val single = Flow[Int].withAttributes(Attributes.inputBuffer(initial = 1, max = 1))

  def debug[T] = Flow[T].map { x => println(x); x }

  val fastSrc =
    Source(() => Iterator from 0)
      .named("fast_source") // the AkkaFlowMaterializer will create actors,
                            // every stage (step) in the flow will be an Actor
                            // `named` sets the name of the actor

  val toTenSrc = Source(1 to 10)

  "IteratorSource" should "count to ten" in {
    fastSrc
      .via(single)
      .take(10)
      .via(debug)
      .runFold(0)((c, _) => c + 1)
      .futureValue shouldBe 10
  }

  it should "log elements using implicit LoggingAdapter" in {
    toTenSrc
      .log("one_to_ten")
      .runForeach(_ => ())
  }


}

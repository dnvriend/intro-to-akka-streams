package com.github.dnvriend.streams

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source

import scala.concurrent.Future

class OverflowStrategyTest extends TestSpec {

  val toTenSrc = Source(1 to 10)

  def overFlowStrategy(overflowStrategy: OverflowStrategy, name: String, buffer: Int = 1): Future[Int] =
    toTenSrc.log(name).buffer(buffer, overflowStrategy).log("after_buffer").runFold(0)((c, _) => c + 1)

  /**
   * OverflowStrategy.backpressure:
   *
   * If the buffer is full when a new element is available
   * this strategy backpressures the upstream publisher until
   * space becomes available in the buffer.
   *
   * Note: No elements will be dropped
   */
  "OverflowStrategyTest" should "OverflowStrategy.backpressure" in {
    overFlowStrategy(OverflowStrategy.backpressure, "backpressure").futureValue shouldBe 10
  }

  /**
   * OverflowStrategy.dropHead:
   *
   * If the buffer is full when a new element arrives,
   * drops the oldest element from the buffer to make space for
   * the new element.
   *
   * Note: Some elements could be dropped
   */
  it should "OverflowStrategy.dropHead" in {
    overFlowStrategy(OverflowStrategy.dropHead, "dropHead").futureValue should be <= 10
  }

  /**
   * OverflowStrategy.dropTail:
   *
   * If the buffer is full when a new element arrives,
   * drops the youngest element from the buffer to make space for
   * the new element.
   *
   * Note: Some elements could be dropped
   */
  it should "OverflowStrategy.dropTail" in {
    overFlowStrategy(OverflowStrategy.dropTail, "dropTail").futureValue should be <= 10
  }

  /**
   * OverflowStrategy.dropBuffer:
   *
   * If the buffer is full when a new element arrives,
   * drops all the buffered elements to make space for the new element.
   */
  it should "OverflowStrategy.dropBuffer" in {
    overFlowStrategy(OverflowStrategy.dropBuffer, "dropBuffer").futureValue shouldBe 10
  }

  /**
   * OverflowStrategy.fail:
   *
   * If the buffer is full when a new element is available
   * this strategy completes the stream with failure.
   */
  it should "OverflowStrategy.fail" in {
    intercept[RuntimeException] {
      overFlowStrategy(OverflowStrategy.fail, "fail", buffer = 0).futureValue
    }
  }
}

/*
 * Copyright 2015 Dennis Vriend
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

package com.github.dnvriend.streams.customstage

import akka.stream.stage._
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ Attributes, FlowShape, Inlet, Outlet }
import com.github.dnvriend.streams.TestSpec

class Ex4StatefulStageTest extends TestSpec {

  /**
   * `StatefulStage` is a `PushPullStage` that provides convenience methods to make some things easier.
   * The behavior is defined in `StageState` instances.
   *
   * The `initial` behavior is specified by subclassing `StatefulStage` and implementing
   * the `initial` method.
   *
   * The behavior can be changed by using become. Use `emit` or `emitAndFinish` to push more than one element
   * from StageState.onPush or StageState.onPull.
   *
   * Use `terminationEmit` to push final elements from `onUpstreamFinish` or `onUpstreamFailure`.
   */

  "CustomDuplicatorStage" should "be implemented with a StatefulStage" in {

    /**
     * The custom duplicator stage does exactly what you think, it emits two elements
     * when one is received. Lets test it:
     */
    class CustomDuplicatorStage[A]() extends StatefulStage[A, A] {
      override def initial: StageState[A, A] = new StageState[A, A] {
        override def onPush(elem: A, ctx: Context[A]): SyncDirective =
          emit(List(elem, elem).iterator, ctx)
      }
    }

    /**
     * To use the custom transformation stage, call `transform()` on a `Flow` or `Source`
     * which takes a factory function returning a Stage: `f: () => Stage`
     *
     * In the example below we use a TestProbe as the Source that generates demand and
     * does assertions.
     */
    withIterator(1) { src ⇒
      src.take(2)
        .transform(() ⇒ new CustomDuplicatorStage)
        .runWith(TestSink.probe[Int])
        .request(Int.MaxValue)
        .expectNext(1, 1, 2, 2)
        .expectComplete()
    }
  }

  it should "be used for other stateful things also" in {
    class SumEvenAndUnevenNumbersCollector(p: Int ⇒ Boolean) extends StatefulStage[Int, (Int, Int)] {
      var sumEven: Int = 0
      var sumUnEven: Int = 0

      override def initial: StageState[Int, (Int, Int)] = new StageState[Int, (Int, Int)] {
        override def onPush(elem: Int, ctx: Context[(Int, Int)]): SyncDirective =
          if (p(elem)) {
            val tuple = (sumEven, sumUnEven)
            sumEven = 0
            sumUnEven = 0
            ctx.push(tuple)
          } else {
            if (elem % 2 != 0) {
              sumUnEven = sumUnEven + elem
              ctx.pull()
            } else {
              sumEven = sumEven + elem
              ctx.pull()
            }
          }
      }
    }

    /**
     * To use the custom transformation stage, call `transform()` on a `Flow` or `Source`
     * which takes a factory function returning a Stage: `f: () => Stage`
     *
     * In the example below we use a TestProbe as the Source that generates demand and
     * does assertions.
     */
    withIterator(1) { src ⇒
      src.take(20)
        .transform(() ⇒ new SumEvenAndUnevenNumbersCollector(_ % 10 == 0)) // emit every 10 elements
        .runWith(TestSink.probe[(Int, Int)])
        .request(Int.MaxValue)
        .expectNext((20, 25), (60, 75))
        .expectComplete()
    }
  }

  it should "be implemented as a GraphShape" in {
    // as the StatefulStage will be deprecated, let's look at how to handle state in a GraphShape

    class CustomDuplicatorStage[A]() extends GraphStage[FlowShape[A, A]] {

      val in = Inlet[A]("Duplicator.in")
      val out = Outlet[A]("Duplicator.out")

      override def shape: FlowShape[A, A] = FlowShape.of(in, out)

      override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
        // note: all mutable state must be inside the GraphStageLogic
        var lastElem: Option[A] = None

        setHandler(in, new InHandler {
          override def onPush(): Unit = {
            val elem = grab(in)
            lastElem = Some(elem)
            push(out, elem)
          }

          override def onUpstreamFinish(): Unit = {
            if (lastElem.isDefined) emit(out, lastElem.get)
            complete(out)
          }
        })

        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            if (lastElem.isDefined) {
              push(out, lastElem.get)
              lastElem = None
            } else {
              pull(in)
            }
          }
        })
      }
    }

    withIterator(1) { src ⇒
      src.take(2)
        .via(new CustomDuplicatorStage)
        .runWith(TestSink.probe[Int])
        .request(Int.MaxValue)
        .expectNext(1, 1, 2, 2)
        .expectComplete()
    }
  }
}

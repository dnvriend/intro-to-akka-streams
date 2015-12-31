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

import akka.stream.impl.fusing.GraphStages
import akka.stream.stage._
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ Attributes, FlowShape, Inlet, Outlet }
import com.github.dnvriend.streams.TestSpec

class Ex1IdentityStageTest extends TestSpec {

  /**
   * Custom transformation stages can be created when you need some kind of processing
   * logic inside a stage that is not part of the standard processing capabilities of akka-streams.
   *
   * For an overview of what comes out of the box have a look at:
   * http://doc.akka.io/docs/akka-stream-and-http-experimental/2.0.1/stages-overview.html
   *
   * Say we just want to learn how to use the PushPullStage, which is the most elementary transformation stage
   * available in akka-streams, and implement some custom logic, just to learn, we can extend the stage and
   * implement our logic inside the stage. Let's create an `identity` stage, that just forwards an element when
   * received to the next stage. Not a whole lot to it, let's take a look:
   */

  "CustomIdentityStage" should "be implemented with a PushPullStage" in {

    /**
     * A custom identity stage that takes elements of type `A`, and it extends the
     * PushPullStage, which has inputs and outputs, so it takes `A` as input, and outputs the same `A`
     */
    class CustomIdentityStage[A] extends PushPullStage[A, A] {
      /**
       * Forward the element when there is demand, conceptually:
       *
       * Source ~> CustomIdentityStage ~> Sink
       *
       * When the Sink generates demand, forward the element from the Source with no processing.
       */
      override def onPush(elem: A, ctx: Context[A]): SyncDirective =
        ctx.push(elem)

      /**
       * request more elements from upstream, conceptually:
       *
       * Source ~> CustomIdentityStage ~> Sink
       *
       * When the Sink generates demand, forward the demand to the Source so it will
       * emit a new Element of type `A`
       */
      override def onPull(ctx: Context[A]): SyncDirective =
        ctx.pull() // request for more elements from upstream (other stages before us)
    }

    /**
     * To use the custom transformation stage, call `transform()` on a `Flow` or `Source`
     * which takes a factory function returning a Stage: `f: () => Stage`
     *
     * In the example below we use a TestProbe as the Source that generates demand and
     * does assertions.
     */
    withIterator() { src ⇒
      src.take(2)
        .transform(() ⇒ new CustomIdentityStage)
        .runWith(TestSink.probe[Int])
        .request(Int.MaxValue)
        .expectNext(0, 1)
        .expectComplete()
    }
  }

  it should "also be implemented using the PushStage" in {
    /**
     * When the stage just propagates the pull upwards to the `previous` stage, it is not necessary to override
     * the onPull handler at all. Such transformations are better of by extending the `PushStage`. The conceptual
     * mapping will still be the same.
     *
     * The reason to use `PushStage` is not just cosmetic. Internal optimizations rely on the fact that the
     * `onPull` method only calls `ctx.pull()` and allow the environment do process elements faster than without
     * this knowledge. By extending `PushStage` the environment can be sure that `onPull()` was not overridden since
     * it is final on `PushStage`.
     */

    class CustomIdentityStage[A] extends PushStage[A, A] {
      override def onPush(elem: A, ctx: Context[A]): SyncDirective =
        ctx.push(elem)
    }

    /**
     * To use the custom transformation stage, call `transform()` on a `Flow` or `Source`
     * which takes a factory function returning a Stage: `f: () => Stage`
     *
     * In the example below we use a TestProbe as the Source that generates demand and
     * does assertions.
     */
    withIterator() { src ⇒
      src.transform(() ⇒ new CustomIdentityStage)
        .take(2)
        .runWith(TestSink.probe[Int])
        .request(Int.MaxValue)
        .expectNext(0, 1)
        .expectComplete()
    }
  }

  it should "also be implemented as a GraphStage" in {
    /**
     * The `GraphStage` abstraction can be used to create arbitrary graph processing stages with any number of input
     * or output ports. It is a counterpart of the GraphDSL.create() method which creates new stream processing stages
     * by composing others. Where GraphStage differs is that it creates a stage that is itself not divisible into
     * smaller ones, and allows state to be maintained inside it in a safe way.
     */

    class CustomIdentityStage[A] extends GraphStage[FlowShape[A, A]] {
      val in = Inlet[A]("Identity.in")
      val out = Outlet[A]("Identity.out")

      override def shape: FlowShape[A, A] = FlowShape.of(in, out)

      override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
        setHandler(in, new InHandler {
          override def onPush(): Unit =
            push(out, grab(in))
        })

        setHandler(out, new OutHandler {
          override def onPull(): Unit = pull(in)
        })
      }
    }

    withIterator() { src ⇒
      src.take(2)
        .via(new CustomIdentityStage)
        .runWith(TestSink.probe[Int])
        .request(Int.MaxValue)
        .expectNext(0, 1)
        .expectComplete()
    }
  }

  it should "already be implemented in the akka stream API" in {
    withIterator() { src ⇒
      src.take(2)
        .via(GraphStages.identity)
        .runWith(TestSink.probe[Int])
        .request(Int.MaxValue)
        .expectNext(0, 1)
        .expectComplete()
    }
  }
}

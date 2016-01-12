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

package com.github.dnvriend.streams.customstage

import akka.stream.stage._
import akka.stream.testkit.scaladsl.TestSink
import com.github.dnvriend.streams.TestSpec

class Ex2CustomMapTest extends TestSpec {

  "CustomMapStage" should "be implemented with a PushPullStage" in {

    /**
     * A custom map stage that takes elements of type `A`, and converts the element to type 'B'.
     * The `CustomMapStage` does this by using the supplied function that converts `A` into `B`
     *
     * The PushPull stage has input and output ports. The `input` ports are the callback methods,
     * `onPush(elem,ctx)` and `onPull(ctx)`. The `output` ports are implemented as methods on
     * on the `Context` object that is supplied as a parameter on the event handler methods below.
     *
     * By calling exactly one "output port" method we wire up these four ports in various ways.
     * `Calling` an output port is called wireing, because the element that has been supplied by the akka-streams
     * runtime to the PushPullStage by calling the `onPush(in,ctx)` or the `onPull(ctx)` method is passed to exactly
     * one output port.
     *
     * The CustomMapStage calls `ctx.push()` from the `onPush()` event handler and it also calls
     * `ctx.pull()` from the `onPull` handler resulting in the conceptual wiring:
     *
     * +---------------------------------+
     * | onPush(in,ctx)    ctx.push(out) |
     * O-------------> f(in) -->---------O
     * |                                 |
     * O-------------<------<------------O
     * | ctx.pull()          onPull(ctx) |
     * +---------------------------------+
     *
     * Map is a typical example of a one-to-one transformation of a stream, the element will be processed and
     * forwarded.
     *
     * Note:
     * A map is just a function from f: A => B, so we will extend the PushPullStage to create this map function
     */
    class CustomMapStage[A, B](f: A ⇒ B) extends PushPullStage[A, B] {
      override def onPush(elem: A, ctx: Context[B]): SyncDirective =
        ctx.push(f(elem)) // transform the element and pushes it downstream when there is demand for it

      override def onPull(ctx: Context[B]): SyncDirective =
        ctx.pull() // request for more elements from upstream (other stages before us)
    }

    /**
     * To use the custom transformation stage, call `transform()` on a `Flow` or `Source`
     * which takes a factory function returning a Stage: `f: () => Stage`
     *
     * In the example below we use a TestProbe as the Source that generates demand and
     * does assertions.
     */
    withIterator(1) { src ⇒
      src.transform(() ⇒ new CustomMapStage(_ * 2))
        .take(2)
        .runWith(TestSink.probe[Int])
        .request(Int.MaxValue)
        .expectNext(2, 4)
        .expectComplete()
    }
  }

  it should "also be implemented using the PushStage" in {
    /**
     * When the stage just propagates the pull upwards to the `previous` stage, it is not necessary to override
     * the onPull handler at all. Such transformations are better of by extending the `PushStage`. The conceptual
     * mapping will still be the same.
     */

    class CustomMapStage[A, B](f: A ⇒ B) extends PushStage[A, B] {
      override def onPush(elem: A, ctx: Context[B]): SyncDirective =
        ctx.push(f(elem)) // transform the element and pushes it downstream when there is demand for it
    }

    /**
     * To use the custom transformation stage, call `transform()` on a `Flow` or `Source`
     * which takes a factory function returning a Stage: `f: () => Stage`
     *
     * In the example below we use a TestProbe as the Source that generates demand and
     * does assertions.
     */
    /**
     * To use the custom transformation stage, call `transform()` on a `Flow` or `Source`
     * which takes a factory function returning a Stage: `f: () => Stage`
     *
     * In the example below we use a TestProbe as the Source that generates demand and
     * does assertions.
     */
    withIterator(1) { src ⇒
      src.transform(() ⇒ new CustomMapStage(_ * 2))
        .take(2)
        .runWith(TestSink.probe[Int])
        .request(Int.MaxValue)
        .expectNext(2, 4)
        .expectComplete()
    }
  }
}

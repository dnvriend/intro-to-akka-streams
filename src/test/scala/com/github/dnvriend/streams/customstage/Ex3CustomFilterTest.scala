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

import akka.stream.scaladsl.Source
import akka.stream.stage.{ PushStage, SyncDirective, Context, PushPullStage }
import akka.stream.testkit.scaladsl.TestSink
import com.github.dnvriend.streams.TestSpec

class Ex3CustomFilterTest extends TestSpec {
  "CustomFilterStage" should "be implemented with a PushPullStage" in {

    /**
     * A custom filter stage that, if the given predicate matches the current element, the element will be
     * forwarded/propagating downwards, otherwise we return the "ball" to our upstream so that we get a new element.
     *
     * This behavior is achieved by modifying the `CustomMapStage` from `Ex2CustomMapTest` example by adding a
     * conditional in the `onPush` handler and decide between a `ctx.pull()` or `ctx.push(elem)` call and results
     * in the following conceptual wiring:
     *
     * +---------------------------------+
     * | onPush(in,ctx)    ctx.push(out) |
     * O----+---->  if p(in)  -->--------O
     * |    | if !p(in)                  |
     * O--<-v--------<------<------------O
     * | ctx.pull()          onPull(ctx) |
     * +---------------------------------+
     */
    class CustomFilterStage[A](p: A ⇒ Boolean) extends PushPullStage[A, A] {
      override def onPush(elem: A, ctx: Context[A]): SyncDirective =
        if (p(elem)) ctx.push(elem) else ctx.pull()

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
    Source(1 to 10).transform(() ⇒ new CustomFilterStage(_ % 2 == 0))
      .runWith(TestSink.probe[Int])
      .request(5)
      .expectNext(2, 4, 6, 8, 10)
      .expectComplete()
  }

  it should "also be implemented using the PushStage" in {
    /**
     * When the stage just propagates the pull upwards to the `previous` stage, it is not necessary to override
     * the onPull handler at all. Such transformations are better of by extending the `PushStage`. The conceptual
     * mapping will still be the same.
     */

    class CustomFilterStage[A](p: A ⇒ Boolean) extends PushStage[A, A] {
      override def onPush(elem: A, ctx: Context[A]): SyncDirective =
        if (p(elem)) ctx.push(elem) else ctx.pull()
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
    Source(1 to 10).transform(() ⇒ new CustomFilterStage(_ % 2 == 0))
      .runWith(TestSink.probe[Int])
      .request(5)
      .expectNext(2, 4, 6, 8, 10)
      .expectComplete()
  }
}

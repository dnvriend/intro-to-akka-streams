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

package com.github.dnvriend.streams.nesting

import akka.stream.scaladsl.Source
import com.github.dnvriend.streams.TestSpec

import scala.concurrent.Future

class FlatteningStagesTest extends TestSpec {

  it should "flatten and concat all sub-streams and output the result" in withIterator(1) { src ⇒
    src.take(3).flatMapConcat { i ⇒
      Source.fromFuture(Future(i)).map(_ + 1)
    }.testProbe { tp ⇒
      tp.request(Long.MaxValue)
      tp.expectNext(2, 3, 4)
      tp.expectComplete()
    }
  }
}

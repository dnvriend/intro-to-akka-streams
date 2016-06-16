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

package com.github.dnvriend.streams.collection

import com.github.dnvriend.streams.TestSpec

class SourceFromCollectionTest extends TestSpec {
  "null" should "throw NullPointerException" in {
    intercept[NullPointerException] {
      fromCollection[Nothing](null) { tp ⇒
        tp.request(1)
        tp.expectComplete()
      }
    }.getMessage should include("Element must not be null, rule 2.13")
  }

  "Option" should "when empty, complete" in {
    fromCollection(Option.empty[String].toList) { tp ⇒
      tp.request(1)
      tp.expectComplete()
    }
  }

  it should "emit a single element then complete" in {
    fromCollection(Option("a").toList) { tp ⇒
      tp.request(1)
      tp.expectNext("a")
      tp.expectComplete()
    }
  }

  "List" should "when empty, complete" in {
    fromCollection(List.empty[String]) { tp ⇒
      tp.request(1)
      tp.expectComplete()
    }

    fromCollection[Nothing](List()) { tp ⇒
      tp.request(1)
      tp.expectComplete()
    }

    fromCollection[Nothing](Nil) { tp ⇒
      tp.request(1)
      tp.expectComplete()
    }
  }

  it should "emit onError when processing list of null values" in {
    fromCollection(List.fill(3)(null)) { tp ⇒
      tp.request(1)
      tp.expectError().getMessage should include("Element must not be null, rule 2.13")
    }
  }

  it should "emit onError when processing list containing null values" in {
    fromCollection(List("a", null, "b")) { tp ⇒
      tp.request(1)
      tp.expectNext("a")
      tp.request(1)
      tp.expectError().getMessage should include("Element must not be null, rule 2.13")
    }
  }

  it should "emit three elements then complete" in {
    fromCollection(List("a", "b", "c")) { tp ⇒
      tp.request(1)
      tp.expectNext("a")
      tp.request(1)
      tp.expectNext("b")
      tp.request(1)
      tp.expectNext("c")
      tp.expectComplete()
    }
  }

  "Vector" should "when empty, complete" in {
    fromCollection(Vector.empty[String]) { tp ⇒
      tp.request(1)
      tp.expectComplete()
    }
  }

  it should "emit three elements then complete" in {
    fromCollection(Vector("a", "b", "c")) { tp ⇒
      tp.request(1)
      tp.expectNext("a")
      tp.request(1)
      tp.expectNext("b")
      tp.request(1)
      tp.expectNext("c")
      tp.expectComplete()
    }
  }

  "Set" should "when empty, complete" in {
    fromCollection(Set.empty[String]) { tp ⇒
      tp.request(1)
      tp.expectComplete()
    }
  }

  it should "emit three elements then complete" in {
    fromCollection(Set("a", "b", "c")) { tp ⇒
      tp.request(1)
      tp.expectNext("a")
      tp.request(1)
      tp.expectNext("b")
      tp.request(1)
      tp.expectNext("c")
      tp.expectComplete()
    }
  }
}

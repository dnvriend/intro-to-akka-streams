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

package com.github.dnvriend.streams.io

import akka.stream.scaladsl.{ Sink, Source }
import com.github.dnvriend.streams.TestSpec

class FileIOTest extends TestSpec {
  trait Foo
  case class ImportStarted(fileName: String, processId: String) extends Foo
  case class ImportFinished(a: String = "") extends Foo
  case class ImportFailed(t: Throwable) extends Foo
  case class NestedType2(a: String = "") extends Foo
  case class NestedType1(b: String = "") extends Foo
  case class RootType(c: String = "") extends Foo

  case class ImportFileCommand(processId: String = "abcdefg", fileName: String = "fileName.xml")
  it should "import" in {
    // import proces

    def unmarshaller(fileName: String, processId: String) =
      Source(List(ImportStarted(fileName, processId), NestedType2(), NestedType1(), RootType(), ImportFinished()))

    Source(List.fill(1)(ImportFileCommand()))
      .flatMapConcat { cmd ⇒
        unmarshaller(cmd.fileName, cmd.processId)
          .map {
            //            case _: NestedType2 ⇒ throw new RuntimeException("error")
            case e ⇒ e
          }
      }
      .recover {
        case t: Throwable ⇒ ImportFailed(t)
      }
      .runWith(Sink.seq).futureValue should matchPattern {
        case Seq(ImportStarted("fileName.xml", "abcdefg"), NestedType2(_), ImportFailed(_))                                ⇒
        case Seq(ImportStarted("fileName.xml", "abcdefg"), NestedType2(_), NestedType1(_), RootType(_), ImportFinished(_)) ⇒
      }
  }
}

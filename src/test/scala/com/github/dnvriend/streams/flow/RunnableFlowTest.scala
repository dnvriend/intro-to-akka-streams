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

package com.github.dnvriend.streams.flow

import akka.stream.scaladsl._
import com.github.dnvriend.streams.TestSpec

import scala.concurrent.Future

class RunnableFlowTest extends TestSpec {
  /**
   *
   * It is possible to attach a Flow to a Source resulting in a composite source,
   * and it is also possible to prepend a Flow to a Sink to get a new sink.
   *
   * After a stream is properly terminated by having both a source and a sink, it will be
   * represented by the RunnableFlow type, indicating that it is ready to be executed.
   *
   * It is important to remember that even after constructing the RunnableFlow by connecting
   * all the source, sink and different processing stages, no data will flow through it until
   * it is 'materialized'.
   *
   * Materialization is the process of allocating all resources needed to run the computation
   * described by a Flow (in Akka Streams this will often involve starting up Actors).
   *
   * Thanks to Flows being simply a description of the processing pipeline they are immutable, thread-safe,
   * and freely shareable, which means that it is for example safe to share and send them between actors,
   * to have one actor prepare the work, and then have it be materialized at some completely different place in the code.
   */

  "RunnableFlow" should "be defined" in {
    val source: Source[Int, Unit] = Source(1 to 10)
    val sink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)

    // connect the Source to the Sink, obtaining a RunnableFlow, which is
    // a Model of the processing pipeline
    val runnable: RunnableGraph[Future[Int]] = source.toMat(sink)(Keep.right)

    // materialize the flow (convert the RunnableFlow model to a runtime representation
    // using the ActorFlowMaterializer which creates a network of actors that will give the
    // behavior defined by the model) and get the value of the FoldSink
    val sum: Future[Int] = runnable.run()

    // create a new processing pipeline, run the network with the result from the future
    Source(sum).map(_ * 2).runWith(Sink.foreach(println))

    sum.futureValue shouldBe 55
  }

  /**
   * After running (materializing) the RunnableFlow[T] we get back the materialized value of type T.
   *
   * Every stream processing stage can produce a materialized value, and it is the responsibility of
   * the user to combine them to a new type.
   *
   * In the above example we used 'toMat' to indicate that we want to 'transform the materialized
   * value of the source and sink', and we used the convenience function Keep.right to say that we are
   * only interested in the materialized value of the sink.
   *
   * In our example the FoldSink materializes a value of type Future which will represent the result
   * of the folding process over the stream.
   *
   * In general, a stream can expose multiple materialized values, but it is quite common to be interested
   * in only the value of the Source or the Sink in the stream.
   *
   * For this reason there is a convenience method called runWith() available for Sink, Source or Flow requiring,
   * respectively, a supplied Source (in order to run a Sink), a Sink (in order to run a Source) or both a Source
   * and a Sink (in order to run a Flow, since it has neither attached yet).
   */

  /**
   * Defining sources, sinks and flows
   *
   * The objects Source and Sink define various ways to create sources and sinks of elements.
   *
   * The following examples show some of the most useful constructs (refer to the API documentation for more details):
   */

  "Sources" should "be created" in {
    // Create a source from an Iterable
    val s1: Source[Int, Unit] = Source(List(1, 2, 3))

    // Create a source from a Future
    val s2: Source[String, Unit] = Source(Future.successful("Hello Streams!"))

    // Create a source from a single element
    val s3: Source[String, Unit] = Source.single("only one element")

    // an empty source
    val s4: Source[String, Unit] = Source.empty[String]
  }

  "Sinks" should "be created" in {
    // Sink that folds over the stream and returns a Future
    // of the final result as its materialized value
    val s1: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)

    // Sink that returns a Future as its materialized value,
    // containing the first element of the stream
    val s2: Sink[Int, Future[Int]] = Sink.head[Int]

    // A Sink that consumes a stream without doing anything with the elements
    val s3: Sink[Any, Future[Unit]] = Sink.ignore

    // A Sink that executes a side-effecting call for every element of the stream
    val s4: Sink[String, Future[Unit]] = Sink.foreach[String](println(_))
  }

  /**
   * There are various ways to wire up different parts of a stream, the following examples
   * show some of the available options:
   */

  "Streams" should "be wired up from different parts" in {
    // Explicitly creating and wiring up a Source, Sink and Flow
    // the Sink is of type Sink[Int, Future[Unit]]
    val runnable: RunnableGraph[Unit] =
      Source(1 to 6)
        .via(
          Flow[Int].map(_ * 2)
        )
        .to(
          Sink.foreach(println(_))
        )

    // Starting from a Source
    val source = Source(1 to 6).map(_ * 2)
    val runnable2: RunnableGraph[Unit] =
      source
        .to(Sink.foreach(println(_)))

    // Starting from a Sink
    val sink: Sink[Int, Unit] = Flow[Int].map(_ * 2).to(Sink.foreach(println(_)))
    val runnable3: RunnableGraph[Unit] =
      Source(1 to 6)
        .to(sink)
  }

}

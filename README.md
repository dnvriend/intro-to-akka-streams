# Introduction to Akka Streams
> Stream processing is a different paradigm to the Actor Model or to Future composition, therefore it may take some 
> careful study of this subject until you feel familiar with the tools and techniques.
-- <cite>Akka Streams Documentation</cite>

[<img src="https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-xml_2.11.svg?label="/>]

## Documentation
- [Akka Streams Documentation](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-RC2/scala.html)
- [Quick Start - Reactive Tweets](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-RC2/scala/stream-quickstart.html#stream-quickstart-scala)
- [Akka Streams API](http://doc.akka.io/api/akka-stream-and-http-experimental/1.0-RC2/)
- [Design Principles behind Reactive Streams](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-RC2/stream-design.html#stream-design)
- [Streams Cookbook](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-RC2/scala/stream-cookbook.html#stream-cookbook-scala)
- [Overview of built-in stages and their semantics](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-RC2/stages-overview.html#stages-overview)
- [Integrating with Actors, external services and reactive streams](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-RC2/scala/stream-integrations.html)
- [Reactive Streams](http://www.reactive-streams.org/)

## Blogs
- [Jon Brisbin - The Reactive Streams Project: Tribalism as a Force for Good](http://jbrisbin.com/post/82994020622/the-reactive-streams-project-tribalism-as-a-force)
- [Adam Warski - Reactive Queue with Akka Reactive Streams](http://www.warski.org/blog/2014/06/reactive-queue-with-akka-reactive-streams/)
- [Boldradius - Introduction to Akka Streams](http://boldradius.com/blog-post/VS0NpTAAADAACs_E/introduction-to-akka-streams)
- [Scraping Reddit with Akka Streams 1.0](https://github.com/pkinsky/akka-streams-example)

## Testing
- [Testing Streams](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-RC1/scala/stream-testkit.html)

## Slides
- [Konrad Malawski - Reactive Streams / Akka Streams - GeeCON Prague 2014](http://www.slideshare.net/ktoso/reactive-streams-akka-streams-geecon-prague-2014)
- [Reactive Streams and RabbitMQ](http://www.slideshare.net/mkiedys/reactive-streams-and-rabbitmq)

## Github
- [ScalaConsultants Team Blog - Akka Streams and RabbitMQ](http://blog.scalac.io/2014/06/23/akka-streams-and-rabbitmq.html)
- [Reactive RabbitMq Activator Template](https://github.com/jczuchnowski/rabbitmq-akka-stream#master)

## Activator Template
- [Akka Streams Activator Template](http://www.typesafe.com/activator/template/akka-stream-scala)

## Video
- [Youtube - Introducing Reactive Streams](https://www.youtube.com/watch?v=khmVMvlP_QA)
- [Youtube - Spray and Akka HTTP](https://www.youtube.com/watch?v=o5PUDI4qi10)
- [Youtube - Reactive Stream Processing with Akka Streams](https://www.youtube.com/watch?v=XCP6zg46utU)
- [Youtube - Netflix JavaScript Talks - Async JavaScript with Reactive Extensions](https://www.youtube.com/watch?v=XRYN2xt11Ek)
- [Youtube - Asynchronous Programming at Netflix](https://www.youtube.com/watch?v=gawmdhCNy-A)
- [Youtube - Building Reactive applications with Spring Reactor and the Reactive Streams standard](https://www.youtube.com/watch?v=AvwZEWu5PPc)
- [Youtube - Typesafe - Play All Day: Reactive Streams and Play 3.0](https://www.youtube.com/watch?v=0i0RRZvARkM)
- [Youtube - Dr. Roland Kuhn - Reactive Streams: Handling Data-Flows the Reactive Way](https://www.youtube.com/watch?v=oUnfAcwDQr4)
- [Youtube - Reactive Streams - Tim Harper](https://www.youtube.com/watch?v=xJn2kMHUl6s)
- [Youtube - Reactive microservices with Reactor - Stephane Maldini](https://www.youtube.com/watch?v=lzkBo3YTvAQ)
- [Youtube - Reactive Stream Processing with kafka-rx](https://www.youtube.com/watch?v=S-Ynyel9pkk)
- [Youtube - Technology Hour - Implementing the Reactive Manifesto with Akka - Adam Warski](https://www.youtube.com/watch?v=LXEhQPEupX8)
- [What does it mean to be Reactive? - Erik Meijer](https://www.youtube.com/watch?v=sTSQlYX5DU0)
- [Typesafe - Going Reactive in Java with Typesafe Reactive Platform](https://www.youtube.com/watch?v=y70Z5S2eSIo)
- [Typesafe - Deep Dive into the Typesafe Reactive Platform - Akka and Scala](https://www.youtube.com/watch?v=fMWzKEN6uTY)
- [Typesafe - Deep Dive into the Typesafe Reactive Platform - Activator and Play](https://www.youtube.com/watch?v=EJl9mQ0051g)
- [Typesafe - What Have The Monads Ever Done For Us with Dick Wall](https://www.youtube.com/watch?v=2IYNPUp751g)
- [Typesafe - Deep Dive into the Typesafe Reactive Platform - Ecosystem and Tools](https://www.youtube.com/watch?v=3nNerwsqrQI)

## Reactive Kafka
> Reactive Streams wrapper for Apache Kafka. -- <quote>[Reactive Kafka](https://github.com/softwaremill/reactive-kafka)</quote>

- [Apache Kafka]()
- [GitHub - Reactive Kafka](https://github.com/softwaremill/reactive-kafka)

*Note:* You will need a configured [Apache Kafka](http://kafka.apache.org/) and [Apache Zookeeper](https://zookeeper.apache.org/).

```scala
import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.softwaremill.react.kafka.ReactiveKafka

implicit val materializer = ActorFlowMaterializer()
implicit  val actorSystem = ActorSystem("ReactiveKafka")

val kafka = new ReactiveKafka(host = "localhost:9092", zooKeeperHost = "localhost:2181")
val publisher = kafka.consume("lowercaseStrings", "groupName", new StringDecoder())
val subscriber = kafka.publish("uppercaseStrings", "groupName", new StringEncoder())


Source(publisher).map(_.toUpperCase).to(Sink(subscriber)).run()
```

## Reactive Rabbit
> Reactive Streams driver for AMQP protocol. Powered by RabbitMQ library. -- <quote>[Reactive Rabbit](https://github.com/ScalaConsultants/reactive-rabbit)</quote>

- [RabbitMq](https://www.rabbitmq.com/)
- [GitHub - Reactive Rabbit](https://github.com/ScalaConsultants/reactive-rabbit)
- [Activator Template - RabbitMQ Akka Stream](https://github.com/jczuchnowski/rabbitmq-akka-stream#master)

Note: You will need a RabbitMQ instance and a configured `reactive-rabbit` connection, see the [reference.conf](https://github.com/ScalaConsultants/reactive-rabbit/blob/master/src/main/resources/reference.conf) for more information. Better yet, fire up [Typesafe Activator](https://www.typesafe.com/get-started) and try out the [RabbitMQ Akka Stream](https://github.com/jczuchnowski/rabbitmq-akka-stream) Activator Template.

```scala
import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.{Sink, Source}
import io.scalac.amqp.Connection

// streaming invoices to Accounting Department
val connection = Connection()
val queue = connection.consume(queue = "invoices")
val exchange = connection
                .publish(
                  exchange = "accounting_department",
                  routingKey = "invoices"
                  )

implicit val system = ActorSystem()
implicit val materializer = ActorFlowMaterializer()

// (queue) ~> (sink)
Source(queue).map(_.message).to(Sink(exchange)).run()
```

# RabbitMQ
> RabbitMQ is open source message broker software (sometimes called message-oriented middleware) that implements the Advanced Message Queuing Protocol (AMQP). The RabbitMQ server is written in the Erlang programming language and is built on the Open Telecom Platform framework for clustering and failover. Client libraries to interface with the broker are available for all major programming languages. -- <quote>[Wikipedia](http://en.wikipedia.org/wiki/RabbitMQ)</quote>

- [RabbitMQ Website](http://www.rabbitmq.com/)
- [RabbitMQ Simulator](http://tryrabbitmq.com/)

## Docker
 - [library/rabbitmq](https://registry.hub.docker.com/u/library/rabbitmq/)
 
## GitHub
 - [RabbitMQ](https://github.com/docker-library/docs/tree/master/rabbitmq)
 
# Apache ActiveMQ
> Apache ActiveMQ is an open source message broker written in Java together with a full Java Message Service (JMS) client. It provides "Enterprise Features" which in this case means fostering the communication from more than one client or server. Supported clients include Java via JMS 1.1 as well as several other "cross language" clients. The communication is managed with features such as computer clustering and ability to use any database as a JMS persistence provider besides virtual memory, cache, and journal persistency. -- <quote>[Wikipedia](http://en.wikipedia.org/wiki/Apache_ActiveMQ)</quote>

# RabbitMQ vs ActiveMQ
> RabbitMQ is an AMQP broker, while ActiveMQ is a JMS one. I suggest you read the AMQP [Wikipedia](http://en.wikipedia.org/wiki/Advanced_Message_Queuing_Protocol) article to get an idea of the concepts used in AMQP, which are different than the ones you're familiar in JMS. One of the main difference is that in AMQP a producer sends to an exchange `without knowing the actual message distribution strategy` while in JMS the producer targets either a `queue` or a `topic` (thus being aware of the type of message routing in place). So it's hard to tell what's done better or worse, as the semantics are very different between JMS and AMQP. -- <quote>[Stackoverflow](http://stackoverflow.com/questions/7044157/switching-from-activemq-to-rabbitmq)</quote>

> RabbitMQ's queues and exchanges are all configured via the AMQP protocol so a client library allows you to configure all your destinations and their behavior. ActiveMQ requires specific destination configuration because the JMS spec doesn't cover any of the administration side of things. Besides that, RabbitMQ's system configuration is Erlang-esque, while ActiveMQ is usually configured in XML. So you'll have to get used to the {tuple} and <> lovely syntax. RabbitMQ is usually installed with OS packages,  -- edit (or the `[library/rabbitmq](https://registry.hub.docker.com/u/library/rabbitmq/)` Docker image) -- while ActiveMQ distributions are archives you drop anywhere (or Maven deps you embed into something else). -- <quote>[Stackoverflow](http://stackoverflow.com/questions/7044157/switching-from-activemq-to-rabbitmq)</quote>

> RabbitMQ’s provisioning capabilities make it the perfect communication bus for anyone building a distributed application, particularly one that leverages cloud-based resources and rapid deployment.
-- <quote>[RabbitMQ in Action](http://www.manning.com/videla/)</quote>

## RabbitMQ Video
- [Youtube - RabbitMQ is the new king](https://www.youtube.com/watch?v=kA8rPIDa388)
- [Youtube - RabbitMQ: Message that Just Works (Part 1)](https://www.youtube.com/watch?v=ABGMjX4K0D8)
- [Youtube - RabbitMQ: Message that Just Works (Part 2)](https://www.youtube.com/watch?v=puMLEy5kk2s)
- [Youtube - RabbitMQ: Message that Just Works (Part 3)](https://www.youtube.com/watch?v=bUA0fMJGQBE)
- [Youtube - RabbitMQ: Message that Just Works (Part 4)](https://www.youtube.com/watch?v=LWVYaaBH3NY)
- [Youtube - Reliable Messaging With RabbitMQ](https://www.youtube.com/watch?v=XjuiZM7JzPw)
- [Youtube - What RabbitMQ Can For You](https://www.youtube.com/watch?v=4lDSwfrfM-I)

# Apache Qpid
> Apache Qpid, an open-source (Apache 2.0 licensed) messaging system, implements the Advanced Message Queuing Protocol. It provides transaction management, queuing, distribution, security, management, clustering, federation and heterogeneous multi-platform support.
-- <quote>[Wikipedia](http://en.wikipedia.org/wiki/Apache_Qpid)</quote>

# JMS
> The Java Message Service (JMS) API is a Java Message Oriented Middleware (MOM) API for sending messages between two or more clients. JMS is a part of the Java Platform, Enterprise Edition, and is defined by a specification developed under the Java Community Process as JSR 914. It is a messaging standard that allows application components based on the Java Enterprise Edition (Java EE) to create, send, receive, and read messages. It allows the communication between different components of a distributed application to be loosely coupled, reliable, and asynchronous. 
-- <quote>[Wikipedia](http://en.wikipedia.org/wiki/Java_Message_Service)</quote>

> JMS attempted to solve the lock-in and interoperability problem by providing a common Java API that hides the actual interface to the individual vendor MQ products.
-- <quote>[RabbitMQ in Action](http://www.manning.com/videla/)</quote>

# AMQP
> The Advanced Message Queuing Protocol (AMQP) is an open standard application layer protocol for message-oriented middleware. The defining features of AMQP are message orientation, queuing, routing (including point-to-point and publish-and-subscribe), reliability and security.
-- <quote>[Wikipedia](http://en.wikipedia.org/wiki/Advanced_Message_Queuing_Protocol)</quote>

-- <quote>[AMQP is the IP of business systems](https://www.youtube.com/watch?v=SXZJau292Uw)</quote>

- [Youtube - Understanding AMQP 1.0](https://www.youtube.com/watch?v=SXZJau292Uw)
- [Youtube - Advanced Message Queuing Protocol](https://www.youtube.com/watch?v=lz-aofC3nkU)

# MQTT
> MQTT (formerly Message Queue Telemetry Transport) is a publish-subscribe based "light weight" messaging protocol for use on top of the TCP/IP protocol. It is designed for connections with remote locations where a "small code footprint" is required and/or network bandwidth is limited. The Publish-Subscribe messaging pattern requires a message broker. The broker is responsible for distributing messages to interested clients based on the topic of a message. Andy Stanford-Clark and Arlen Nipper of Cirrus Link Solutions authored the first version of the protocol in 1999. 
-- <quote>[Wikipedia](http://en.wikipedia.org/wiki/MQTT)</quote>

# STOMP
> Simple (or Streaming) Text Oriented Message Protocol (STOMP), formerly known as TTMP, is a simple text-based protocol, designed for working with message-oriented middleware. It provides an interoperable wire format that allows STOMP clients to talk with any message broker supporting the protocol. It is thus language-agnostic, meaning a broker developed for one programming language or platform can receive communications from client software developed in another language. 
-- <quote>[Wikipedia](http://en.wikipedia.org/wiki/Streaming_Text_Oriented_Messaging_Protocol)</quote>

# XMPP
> Extensible Messaging and Presence Protocol (XMPP) is a communications protocol for message-oriented middleware based on XML (Extensible Markup Language). It enables the near-real-time exchange of structured yet extensible data between any two or more network entities. The protocol was originally named Jabber, and was developed by the Jabber open-source community in 1999 for near real-time instant messaging (IM), presence information, and contact list maintenance. Designed to be extensible, the protocol has also been used for publish-subscribe systems, signalling for VoIP, video, file transfer, gaming, Internet of Things (IoT) applications such as the smart grid, and social networking services.
-- <quote>[Wikipedia](http://en.wikipedia.org/wiki/XMPP)</quote>

# Slick with Reactive Streams Support
> Slick is a modern database query and access library for Scala. It allows you to work with stored data almost as if you were using Scala collections while at the same time giving you full control over when a database access happens and which data is transferred. You can write your database queries in Scala instead of SQL, thus profiting from the static checking, compile-time safety and compositionality of Scala. Slick features an extensible query compiler which can generate code for different backends. 
-- <quote>[Slick](http://slick.typesafe.com/)</quote>

- [Slick 3.0 Streaming](http://slick.typesafe.com/doc/3.0.0/dbio.html#streaming)

## Books
- [Protocol specification](https://www.rabbitmq.com/resources/specs/amqp0-9-1.pdf)

## Apache Zookeeper
> Apache ZooKeeper is an effort to develop and maintain an open-source server which enables highly reliable distributed coordination. -- <quote>[Apache Zookeeper](https://zookeeper.apache.org/)</quote>

- [Apache Zookeeper Documentation](https://zookeeper.apache.org/doc/trunk/)

## Apache Kafka
> Apache Kafka is publish-subscribe messaging rethought as a distributed commit log. -- <quote>[Apache Kafka](http://kafka.apache.org/)</quote>

- [Apache Kafka Documentation](http://kafka.apache.org/documentation.html)

## Slick
- [Activator Template - Hello Slick](https://github.com/typesafehub/activator-hello-slick#slick-3.0)
- [Activator Template - Slick Plain SQL](https://github.com/typesafehub/activator-slick-plainsql)

# Introduction
## Blocking
Blocking typically occurs in a "pull" based system. These systems pull data as required. The problem is that when 
there is no data to pull, they often block the thread which is inefficient.

I find it best to think about these problems in terms of plumbing. In our pull scenario, we have a series of pipes 
connected to a water source. We put a pump on the end of the pipes that will pull water through the pipes and empty it 
out at our destination. The problem here is that when we run out of water, the pump doesn't know there is a problem 
and continues to try to pull water. Do this long enough and your pump will burn out.

## Back Pressure
In a "push" based system, it is possible for the producer to create more data than the consumer can handle which can 
cause the consumer to crash.

Our push scenario moves the pump to the other end. Now we are pumping water into our pipes which then flows into a sink 
at the other end. The pump can be triggered by a float so it only works when there is water to pump. The problem is the 
sink is not capped. This means that when we fill it up, the water just overflows and the pump keeps pumping. Also not good.

## Akka Streams
What we need is a system which puts a pump at the water source and also puts a cap on the sink. This means that the 
pump at the source will only run when there is water to pump, and meanwhile the sink will fill up and because it is 
capped it will create back pressure when it is full. The back pressure can trigger the pump to stop pumping again.

This is exactly what Akka Streams does for us. In fact, if you look at the terminology for Akka Streams you will see 
that it lays it out in the exact terms I have been using. The most basic stream in Akka Streams consists of two parts: 
A Source and a Sink.

## Source
![A Source](https://github.com/dnvriend/intro-to-akka-streams/blob/master/img/source.png "A Source")

A Source is the input to the stream. It is from here that all the data will flow. Each Source has a single output channel 
and no input channel. Data flows from the Source, through the output channel, and into whatever might be connected to that 
Source. Examples of Sources could include a database query, an http request, or even something as simple as a random 
number generator. In our analogy, this is our water source, which is connected to our pump. It is drawing water from a 
reservoir and pushing it through our pipes.

## Sink
![A Sink](https://github.com/dnvriend/intro-to-akka-streams/blob/master/img/sink.png "A Sink")

A Sink is the endpoint for the stream. The data from the stream will eventually find it's way to the Sink. A Sink has a 
single input channel and no output channel. Data flows into the input channel and collects in the Sink. Examples of Sink 
behavior could include writing to a database, writing to a file, or aggregating data in memory. This is the capped sink 
in our analogy. Water is flowing through the pipes and eventually collecting in our sink.

## Runnable Flow
![A Runnable Flow](https://github.com/dnvriend/intro-to-akka-streams/blob/master/img/runnable_flow.png "A Runnable Flow")

If you connect a Source to a Sink you get a Runnable Flow. This is the most basic complete form you can make in Akka Streams. 
Your stream is ready to use and data will now flow through it. Until you connect both a Source and a Sink, the data can not flow. 
Again, looking to our analogy, if you have a water source and a pump, but nowhere to pump the water to, then you don't have a 
complete system. Conversely, if you have a sink, but no water to pump into it, then again it isn't a complete system. 
Only when you connect the two do you get a complete system.

## Flow
![A Flow](https://github.com/dnvriend/intro-to-akka-streams/blob/master/img/flow.png "A Flow")

While you can do a lot with just a Source and a Sink, things get more interesting when you add a Flow into the mix. 
A Flow can be used to apply transformations to the data coming out of a Source before putting it into a Sink. The Flow 
then has a single input channel and a single output channel. This allows it to be connected to both a Source and a Sink. 
Connecting a Flow to just a Source gives you a new Source. Connecting a Flow to just a Sink gives you a new Sink. 
Connecting a Source, Flow and Sink gives you a Runnable Flow. For our analogy this is the equivalent of putting a bend 
in the pipes, or perhaps narrowing or widening the pipes to change the flow rate. You are providing some way to alter the 
flow of the water.

## A Chain
![A Chain](https://github.com/dnvriend/intro-to-akka-streams/blob/master/img/chain.png "A Chain")

Because Flows have both an input and an output you can chain them together allowing data to flow from a single Source, 
through multiple Flows and finally into the Sink.

A Runnable Flow, no matter how complex, includes all the facilities for back pressure. Data flows through the system one 
way, but requests for additional data to flow back through the system in the other direction. Under the hood, the Sink 
sends a request back through the Flows to the Source. This request notifies the Source that the Sink is ready to handle 
some more data. The Source will then push a set amount of data through the Flows into the Sink. The Sink will then 
process this data and when it has finished it will send another request for more data. This means that if the Sink gets 
backed up, then the time between those requests will increase and the necessary back pressure is generated.

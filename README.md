# Introduction to Akka Streams
- [Akka Streams Activator Template](http://www.typesafe.com/activator/template/akka-stream-scala)
- [Scraping Reddit with Akka Streams 1.0](https://github.com/pkinsky/akka-streams-example)
- [Introduction to Akka Streams](http://boldradius.com/blog-post/VS0NpTAAADAACs_E/introduction-to-akka-streams)

# Documentation
- [Akka Streams Documentation](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-RC1/scala.html)

# Testing
- [Testing Streams](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-RC1/scala/stream-testkit.html)

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


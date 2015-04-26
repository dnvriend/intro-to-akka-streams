# Introduction to Akka Streams
This is forked from [BoldRadius](http://boldradius.com/) - [Introduction to Akka Streams](http://boldradius.com/blog-post/VS0NpTAAADAACs_E/introduction-to-akka-streams?utm_campaign=Blog&utm_content=14368871&utm_medium=social&utm_source=twitter) by [Walde Waldron](http://boldradius.com/team/U9kfRDIAADEAV8mW/wade)

# Introduction
In big data processing, one of the challenges is how to consume and transform large amounts of data efficiently and 
within a fixed set of resources. There are a few key problems faced when trying to consume the data.

## The first is blocking.
Blocking typically occurs in a "pull" based system. These systems pull data as required. The problem is that when 
there is no data to pull, they often block the thread which is inefficient.

I find it best to think about these problems in terms of plumbing. In our pull scenario, we have a series of pipes 
connected to a water source. We put a pump on the end of the pipes that will pull water through the pipes and empty it 
out at our destination. The problem here is that when we run out of water, the pump doesn't know there is a problem 
and continues to try to pull water. Do this long enough and your pump will burn out.

## The second is back pressure.
In a "push" based system, it is possible for the producer to create more data than the consumer can handle which can 
cause the consumer to crash.

Our push scenario moves the pump to the other end. Now we are pumping water into our pipes which then flows into a sink 
at the other end. The pump can be triggered by a float so it only works when there is water to pump. The problem is the 
sink is not capped. This means that when we fill it up, the water just overflows and the pump keeps pumping. Also not good.

## Akka Streams attempts to solve both of these problems.
What we need is a system which puts a pump at the water source and also puts a cap on the sink. This means that the 
pump at the source will only run when there is water to pump, and meanwhile the sink will fill up and because it is 
capped it will create back pressure when it is full. The back pressure can trigger the pump to stop pumping again.

This is exactly what Akka Streams does for us. In fact, if you look at the terminology for Akka Streams you will see 
that it lays it out in the exact terms I have been using. The most basic stream in Akka Streams consists of two parts: 
A Source and a Sink.

## Source
![A Source](https://github.com/dnvriend/intro-to-akka-streams/blob/master/img/sink.png "A Source")
A Source is the input to the stream. It is from here that all the data will flow. Each Source has a single output channel 
and no input channel. Data flows from the Source, through the output channel, and into whatever might be connected to that 
Source. Examples of Sources could include a database query, an http request, or even something as simple as a random 
number generator. In our analogy, this is our water source, which is connected to our pump. It is drawing water from a 
reservoir and pushing it through our pipes.


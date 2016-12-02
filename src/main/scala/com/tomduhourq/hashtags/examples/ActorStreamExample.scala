package com.tomduhourq.hashtags.examples

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

/**
 * Test actor as stream that only emits messages starting with "Hello"
 * from a Source
 */
object ActorStreamExample extends App {
  implicit val system = ActorSystem("Actor-example-sys")
  implicit val materializer = ActorMaterializer()

  final case class Message(text: String)
  val source = Source.actorRef[Message](2, OverflowStrategy.fail)

  val helloSource = source.filter(message => message.text.startsWith("Hello"))

  val ref = Flow[Message].to(Sink.foreach(println)).runWith(helloSource)

  ref ! Message("Hi there!")
  ref ! Message("Hello there!")
}

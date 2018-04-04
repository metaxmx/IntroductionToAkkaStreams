package com.illucit.examples

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

object SimpleExample extends App {

  implicit val system: ActorSystem = ActorSystem("example")
  implicit val materializer: Materializer = ActorMaterializer()

  val source = Source(List("apple", "pear", "orange"))

  val sink = Sink.ignore

  // Materialize and run stream
  val streamResult = source.runWith(sink)

  // Handle future
  Await.ready(streamResult, atMost = 10 seconds)

  system.terminate()

}

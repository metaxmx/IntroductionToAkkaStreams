package com.illucit.snippets

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}

trait Snippet {

  implicit val system: ActorSystem = ActorSystem("example")

  implicit val materializer: Materializer = ActorMaterializer()

  implicit val executionContext = system.dispatcher

}

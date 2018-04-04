package com.illucit.examples

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.{Get, addHeader, _}
import akka.http.scaladsl.model.headers.Cookie
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}

import scala.language.postfixOps
import scala.util.Success

object HttpClientExample extends App{

  implicit val system: ActorSystem = ActorSystem("example")
  implicit val materializer: Materializer = ActorMaterializer()

  // Import execution context of actor system
  import system.dispatcher

  val httpFlow: Flow[HttpRequest, HttpResponse, Any] = Http().outgoingConnectionHttps("illucit.com", port = 443)

  val request = Get("https://www.illucit.com/impressum/")

  val singleResponse =
    Source.single(request)
      .map(_ ~> addHeader(Cookie("qtrans_front_language" -> "de")))
      .via(httpFlow)
      .mapAsync(1)(Unmarshal(_).to[String])
      .runWith(Sink.head)

  singleResponse onComplete {
    case Success(pageContent) =>
      println("Got page content: \n" + pageContent.take(200) + "...")
      system.terminate()
    case _ =>
      system.terminate()
  }

}

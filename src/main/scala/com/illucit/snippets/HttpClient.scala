package com.illucit.snippets

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.headers.Cookie
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.unmarshalling.{FromResponseUnmarshaller, Unmarshal}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future

class HttpClient extends Snippet {

  val httpFlow: Flow[HttpRequest, HttpResponse, Any] = Http().outgoingConnectionHttps("illucit.com", port = 443)

  // Single Request

  val request = Get("https://www.illucit.com/impressum/")

  val singleResponse: Future[String] =
    Source.single(request)
      .map(_ ~> addHeader(Cookie("qtrans_front_language" -> "de")))
      .via(httpFlow)
      .mapAsync(1)(Unmarshal(_).to[String])
      .runWith(Sink.head)

  // Flow

  val responseFlow: Flow[Uri, String, NotUsed] = Flow[Uri]
    .map(uri => Get(uri))
    .map(_ ~> addHeader(Cookie("qtrans_front_language" -> "de")))
    .via(httpFlow)
    .mapAsync(1)(Unmarshal(_).to[String])

  val responses: Future[Seq[String]] =
    Source(List(Uri("https://www.illucit.com/"), Uri("https://www.illucit.com/robots.txt"), Uri("https://www.illucit.com/sitemap_index.xml")))
      .via(responseFlow)
      .runWith(Sink.seq)

}

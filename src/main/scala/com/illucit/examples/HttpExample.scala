package com.illucit.examples

import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentTypes.{`text/html(UTF-8)`, `text/plain(UTF-8)`}
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.headers.ContentDispositionTypes.attachment
import akka.http.scaladsl.model.headers.`Content-Disposition`
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{FileIO, Framing, Source}
import akka.stream.{ActorMaterializer, IOResult, Materializer}
import akka.util.ByteString

import scala.concurrent.Future

object HttpExample extends App {

  implicit val system = ActorSystem("http")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val loremIpsumFile = Paths.get("src/main/resources/loremipsum.txt")
  val loremIpsumSize = Files.size(loremIpsumFile)

  def loremIpsumSource(): Source[ByteString, Future[IOResult]] = FileIO.fromPath(loremIpsumFile)

  val route: Route =
    pathPrefix("lorem-ipsum") {
      get {
        path("view") {
          complete {
            // Stream with fixed size
            HttpEntity(`text/plain(UTF-8)`, loremIpsumSize, loremIpsumSource())
          }
        } ~
          path("download") {
            respondWithHeader(`Content-Disposition`(attachment, Map("filename" -> "lorem-ipsum.txt"))) {
              // Fold stream in Memory
              val byteDataFuture = loremIpsumSource().runFold(ByteString.empty)(_ ++ _)
              onSuccess(byteDataFuture) { byteData =>
                complete {
                  HttpEntity(`text/plain(UTF-8)`, byteData)
                }
              }
            }
          } ~
          path("stream") {
            complete {
              // Stream file
              HttpEntity(`text/plain(UTF-8)`, loremIpsumSource())
            }
          } ~
          path("stream-words") {
            parameter('words.as[Int] ? 30) { words =>
              complete {
                // Stream words
                val wordsSource = loremIpsumSource()
                  .via(Framing.delimiter(ByteString(" "), maximumFrameLength = 2048, allowTruncation = true))
                  .map(bs => ByteString(bs.utf8String.trim).concat(ByteString(" ")))
                  .take(words)
                HttpEntity(`text/plain(UTF-8)`, wordsSource)
              }
            }
          } ~
          path("stream-sentences") {
            parameter('sentences.as[Int] ? 2) { sentences =>
              complete {
                // Stream sentences
                val sentencesSource = loremIpsumSource()
                  .via(Framing.delimiter(ByteString("."), maximumFrameLength = 2048, allowTruncation = true))
                  .map(bs => ByteString(bs.utf8String.trim).concat(ByteString(". ")))
                  .take(sentences)
                HttpEntity(`text/plain(UTF-8)`, sentencesSource)
              }
            }
          }
      }
    } ~ pathEndOrSingleSlash {
      get {
        complete {
          val index =
            """|<h1>HTTP Example: Lorem Ipsum</h1>
               |<ul>
               |<li><a href="/lorem-ipsum/view">View</a>
               |<li><a href="/lorem-ipsum/download">Download</a>
               |<li><a href="/lorem-ipsum/stream?words=20">Stream file</a>
               |<li><a href="/lorem-ipsum/stream-words?words=20">Stream 20 words</a>
               |<li><a href="/lorem-ipsum/stream-sentences?sentences=3">Stream 3 sentences</a>
               |<li><form action="/stop" method="POST"><button>Stop</button>
               |""".stripMargin
          HttpEntity(`text/html(UTF-8)`, index)
        }
      }
    } ~ path("stop") {
      post {
        complete {
          unbind()
          "OK"
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(Route.seal(route), "localhost", 9000)

  println("Waiting for Requests at localhost:9000")

  def unbind(): Unit = {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => stopSystem())
  }

  def stopSystem(): Unit = {
    system.terminate()
    println("Server stopped")
  }


}

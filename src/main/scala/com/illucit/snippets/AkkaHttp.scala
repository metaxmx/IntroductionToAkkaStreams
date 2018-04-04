package com.illucit.snippets

import java.nio.file.Paths

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString

import scala.concurrent.Future

class AkkaHttp extends Snippet {

  val route: Route = {
    path("url") {
      get {
        complete {
          val source = FileIO.fromPath(Paths.get("myfile.txt"))
          // Stream download from file or other source
          HttpEntity(ContentTypes.`application/octet-stream`, source)
        }
      } ~
      post {
        extractRequestEntity { entity =>
          val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get("myfile.txt"))
          val future: Future[IOResult] = entity.dataBytes.runWith(sink)
          // Stream upload into file or other sink
          onSuccess(future) { streamResult =>
            complete {
              if (streamResult.wasSuccessful) {
                "upload complete"
              } else {
                StatusCodes.InternalServerError -> s"error storing file: ${streamResult.getError.getMessage}"
              }
            }
          }
        }
      }
    }
  }

}

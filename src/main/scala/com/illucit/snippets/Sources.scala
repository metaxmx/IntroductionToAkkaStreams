package com.illucit.snippets

import java.nio.file.Paths

import akka.stream.scaladsl.{FileIO, Source}

import scala.concurrent.Promise

class Sources extends Snippet {

  // Empty
  val sourceEmpty = Source.empty[String]

  // From Iterable
  val sourceIterable = Source(1 to 1000)
  val sourceIterable2 = Source(List(1,2,3,99))
  val sourceIterableInfinite = Source(Stream.from(1))

  // From File
  val sourceFile = FileIO.fromPath(Paths.get("/my/file"))

  // Repeat same value
  val sourceRepeat = Source.repeat("Hello World")

  // From Future
  val promise = Promise[Int]()
  val sourceFuture = Source.fromFuture(promise.future)
  promise.success(1337)

}

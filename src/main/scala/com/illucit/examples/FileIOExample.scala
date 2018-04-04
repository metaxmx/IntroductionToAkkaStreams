package com.illucit.examples

import java.nio.file.Paths
import java.time.LocalDateTime

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Flow, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult, Materializer, ThrottleMode}
import akka.util.ByteString

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object FileIOExample extends App {

  implicit val system: ActorSystem = ActorSystem("example")
  implicit val materializer: Materializer = ActorMaterializer()

  // Import execution context of actor system
  import system.dispatcher

  println("Log the even numbers 1-100000 to a file")

  val source: Source[Int, NotUsed] = Source(1 to 100000)

  val out = Paths.get("out", "streamTo.txt")
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(out)

  println("Build flow ...")

  def filterEven(number: Int): Boolean = number % 2 == 0

  def printNumber(number: Int): ByteString = ByteString(number.toString)

  def addDate(line: ByteString): ByteString =
    ByteString(LocalDateTime.now().toString)
      .concat(ByteString(": "))
      .concat(line)

  def addLineBreak(line: ByteString): ByteString = line.concat(ByteString("\n"))

  val flow: Flow[Int, ByteString, NotUsed] = Flow[Int]
    .throttle(10000, 1 second, 1000, ThrottleMode.shaping)
    .filter(filterEven)
    .map(printNumber)
    .map(addDate)
    .map(addLineBreak)

  val graph: RunnableGraph[Future[IOResult]] = source.via(flow).toMat(sink)(Keep.right)

  println("Starting graph ...")

  val resultFuture: Future[IOResult] = graph.run()

  resultFuture.onComplete {
    case Success(io) if io.status.isSuccess =>
      println(s"Successfully written ${io.count} bytes")
      system.terminate()
    case Success(io) if io.status.isFailure =>
      println(s"Error writing to file, but wrote ${io.count} bytes: ${io.status.failed.get.getMessage}")
      system.terminate()
    case Failure(e) =>
      println(s"Error writing to file: ${e.getMessage}")
      system.terminate()
  }

  println("Main thread finished")

}

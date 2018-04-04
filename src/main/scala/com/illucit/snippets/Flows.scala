package com.illucit.snippets

import java.nio.file.Paths

import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString

class Flows extends Snippet {

  Source(1 to 100000)
    .filter(_ % 2 == 0 /* Even numbers only */)
    .drop(15 /* drop first 15 elements */)
    .map(nr => ByteString(s"[$nr] Line $nr\n") /* Convert each to ByteString as "[33] Line 33\n" */)
    .to(FileIO.toPath(Paths.get("out.txt"))) /* Write to file */
    .run()

  val fruit = Source(List("apple", "banana", "orange"))
  val prices = Source(List("1,33", "2,44", "0,99")).map(_ + "€")

  fruit
    .zipWith(prices)("The " + _ + " costs " + _)
    .runForeach(println)
  // The apple costs 1,33€
  // The banana costs 2,44€
  // The orange costs 0,99€

}

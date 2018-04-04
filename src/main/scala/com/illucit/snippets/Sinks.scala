package com.illucit.snippets

import akka.stream.scaladsl.Sink
import akka.util.ByteString

class Sinks extends Snippet {

  // Ignore values (drain stream)
  val sinkIgnore = Sink.ignore

  // head, headOption, last, lastOption
  val sinkHead = Sink.head[String]
  val sinkHeadOption = Sink.headOption[Int]
  val sinkLast = Sink.last[String]
  val sinkLastOption = Sink.lastOption[String]

  // fold (e.g. count chars)
  val sinkFoldCount = Sink.fold[Int, String](zero = 0)((numChars, nextString) => numChars + nextString.length)
  val sinkFoldConcat = Sink.fold[ByteString, ByteString](ByteString.empty)(_ ++ _)

  // collect all values
  val sinkSeq = Sink.seq[String]

}

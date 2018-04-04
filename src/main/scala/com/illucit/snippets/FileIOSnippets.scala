package com.illucit.snippets

import java.nio.file.Paths

import akka.stream.scaladsl.FileIO

class FileIOSnippets extends Snippet {

  // File source: Read data (ByteString) from file
  val fileSource = FileIO.fromPath(Paths.get("in.dat"), chunkSize = 8192)

  // File sink: Store data (ByteString) into file
  val fileSink = FileIO.toPath(Paths.get("out.dat"))

  // Stream from one file to another
  fileSource.runWith(fileSink)

}

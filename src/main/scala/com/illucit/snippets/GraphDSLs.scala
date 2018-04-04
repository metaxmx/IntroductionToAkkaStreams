package com.illucit.snippets

import akka.NotUsed
import akka.stream.{ClosedShape, FlowShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}

class GraphDSLs extends Snippet {

  val runnableGraph = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val in = Source(1 to 10)
      val out = Sink.ignore

      val bcast = builder.add(Broadcast[Int](2))
      val merge = builder.add(Merge[Int](2))

      val f1, f2, f3, f4 = Flow[Int].map(_ + 10)

      in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
      bcast ~> f4 ~> merge
      ClosedShape
  })

  runnableGraph.run()

  val flowGraph: Flow[Int, String, NotUsed] = Flow.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val in = builder.add(Flow[Int])
      val out = builder.add(Flow[String])

      val bcast = builder.add(Broadcast[Int](2))
      val merge = builder.add(Merge[Int](2))

      val f1, f2, f3, f4 = Flow[Int].map(_ + 10)

      val toStr = Flow[Int].map(_.toString)

      in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> toStr ~> out
      bcast ~> f4 ~> merge
      FlowShape(in.in, out.out)
  })

}

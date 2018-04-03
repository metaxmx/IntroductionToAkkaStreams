package com.illucit.examples

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Status}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, FlowShape, Materializer, OverflowStrategy}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import GraphDSL.Implicits._

object WebSocketExample extends App {

  implicit val system = ActorSystem("http")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  // Actor handling chat

  class ChatRoomActor extends Actor {

    var users: Map[String, ActorRef] = Map.empty[String, ActorRef]

    override def receive: Receive = {
      case UserConnected(name, actorRef) =>
        users += name -> actorRef
        broadcast(SystemMessage(s"User $name joined the chat..."))
        println(s"User $name joined the chat")

      case UserDisconnected(name) =>
        println(s"User $name left the chat")
        broadcast(SystemMessage(s"User $name left the chat"))
        for(actorRef <- users.get(name)) actorRef ! Status.Success(0)
        users -= name

      case msg: UserMessage =>
        broadcast(msg)
    }

    def broadcast(message: BroadcastMessage): Unit = for (actorRef <- users.values) actorRef ! message

  }

  val chatRoomActor = system.actorOf(Props[ChatRoomActor], "ChatRoom")

  // Messages

  sealed trait ChatMessage

  case class UserConnected(username: String, actorRef: ActorRef) extends ChatMessage

  case class UserDisconnected(username: String) extends ChatMessage

  sealed trait BroadcastMessage

  case class SystemMessage(message: String) extends BroadcastMessage

  case class UserMessage(username: String, message: String) extends BroadcastMessage with ChatMessage

  // WebSocket flow

  def webSocketChatFlow(username: String): Flow[Message, Message, AnyRef] = {

    //Factory method allows for materialization of this Source
    Flow.fromGraph(GraphDSL.create(Source.actorRef[BroadcastMessage](bufferSize = 5, OverflowStrategy.fail)) {
      implicit builder =>
        chatSource => //it's Source from parameter

          //flow used as input, it takes Messages
          val fromWebsocket = builder.add(
            Flow[Message].collect {
              case TextMessage.Strict(txt @ "shutdown") =>
                stopSystem()
                UserMessage(username, txt)
              case TextMessage.Strict(txt) => UserMessage(username, txt)
            })

          //flow used as output, it returns Messages
          val backToWebsocket = builder.add(
            Flow[BroadcastMessage].map {
              case UserMessage(author, text) => TextMessage(s"[$author]: $text")
              case SystemMessage(text) => TextMessage(text)
            }
          )

          //send messages to the actor, if sent also UserLeft(user) before stream completes.
          val chatActorSink = Sink.actorRef[ChatMessage](chatRoomActor, UserDisconnected(username))

          //merges both pipes
          val merge = builder.add(Merge[ChatMessage](2))

          //Materialized value of Actor who sits in the chatroom
          val actorAsSource = builder.materializedValue.map(actor => UserConnected(username, actor))

          //Message from websocket is converted into IncommingMessage and should be sent to everyone in the room
          fromWebsocket ~> merge.in(0)

          //If Source actor is just created, it should be sent as UserJoined and registered as particiant in the room
          actorAsSource ~> merge.in(1)

          //Merges both pipes above and forwards messages to chatroom represented by ChatRoomActor
          merge ~> chatActorSink

          //Actor already sits in chatRoom so each message from room is used as source and pushed back into the websocket
          chatSource ~> backToWebsocket

          // expose ports
          FlowShape(fromWebsocket.in, backToWebsocket.out)
    })
  }

  // HTTP Route

  val route: Route = {
    pathEndOrSingleSlash {
      getFromResource("websockets/index.html")
    } ~
      path("websocket-chat") {
        get {
          parameter('username) { username =>
            handleWebSocketMessages(webSocketChatFlow(username))
          }
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

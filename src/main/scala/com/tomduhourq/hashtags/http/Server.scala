package com.tomduhourq.hashtags.http

import java.io.File
import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Flow}
import akka.util.Timeout
import com.tomduhourq.hashtags._

import scala.concurrent.duration._
import scala.io.StdIn

object Server extends App {
  implicit val system = ActorSystem("http-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout: Timeout = 5 seconds

  /**
   * Receives a path and returns an Akka Http route that streams
   * the file on the given directory by chunks
   *
   * @param path the path to get the file from
   * @return the corresponding streaming route
   */
  def completeWithPath(path: String): Route =
    get {
      complete(
        HttpEntity(ContentTypes.`text/plain(UTF-8)`, FileIO.fromPath(Paths.get(path)))
      )
    }

  val TwitterRoutes =
    pathPrefix("tweets") {
      path("blocking") {
        completeWithPath(blockingTweetsPath)
      } ~
      path("streaming") {
        completeWithPath(streamingTweetsPath)
      }
  }


  sealed trait Transaction
  final case class Deposit(name: String, amount: Double) extends Transaction
  final case class Withdraw(name: String, amount: Double) extends Transaction
  val parsed = Flow[List[String]] collect {
    case "Withdraw" :: name :: amount :: _ => Withdraw(name, amount.toDouble)
    case "Deposit" :: name :: amount :: _ => Deposit(name, amount.toDouble)
  }

  type Bank = Map[String, Double]
  def applyTransaction(t: Transaction, bank: Bank): Bank = t match {
    case Deposit(name, amount) => bank.updated(name, bank.getOrElse(name, 0D) + amount)
    case Withdraw(name, amount) => bank.updated(name, bank.getOrElse(name, 0D) - amount)
  }

  // See over json4s transformation
  val parseAndApplyWS =
    Flow[Message].collect {
      case TextMessage.Strict(txt) => txt
    }.via(Flow[String].map(_.split(',').toList)).via(parsed).
      scan(Map.empty[String, Double])((bank, transaction) => applyTransaction(transaction, bank)).
      map(bank => TextMessage.Strict(bank.toString))

  val WebSocketRoutes =
    get {
      path(PathEnd) {
        println("/ executed")
        getFromResource("bank/ws_bank.html")
      } ~
      path("ws") {
        println("/ws executed")
        handleWebSocketMessages(parseAndApplyWS)
      }
    }

  val host = "0.0.0.0"
  val bindHttpRoutes = Http().bindAndHandle(TwitterRoutes ~ WebSocketRoutes, host, 8080)

  println(s"Server online at http://$host:8080")
  StdIn.readLine()
  bindHttpRoutes flatMap(_.unbind) onComplete(_ => system.terminate)
}

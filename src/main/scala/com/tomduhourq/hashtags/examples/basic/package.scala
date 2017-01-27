package com.tomduhourq.hashtags.examples

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import com.tomduhourq.hashtags.configuration.AkkaSystemConfiguration

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

package object basic extends AkkaSystemConfiguration {

  // sbt console -Dscala.color
  // 1) get the file up with an io.Source
  def transactionsFile = io.Source.fromFile("src/main/resources/examples/basic.csv").getLines()

  // 2) create our first source
  val transactionSource = Source.fromIterator(() => transactionsFile)

  // First test
  transactionSource.runForeach(println) // Same as transactionSource.runWith(Sink.foreach(println))

  // 3) Our first flow, just split by commas and get the list
  val transactionRowToList = (line: String) => line.split(",").toList
  val allRowsFlow = Flow.fromFunction(transactionRowToList)

  // Second test: we got the header :/
  transactionSource.via(allRowsFlow).runForeach(println)
  //transactionSource.map(_.split(",").toList).runWith(Sink.foreach(println))
  //transactionSource.map(transactionRowToList).runForeach(println)

  // Let's take the header out
  val rows = Flow[String] drop 1 map transactionRowToList

  // Third test: we now have the two rows
  transactionSource.via(rows).runForeach(println)

  // 5) lets create the model (:paste) - we want case classes :)
  sealed trait Transaction
  final case class Deposit(name: String, amount: Double) extends Transaction
  final case class Withdraw(name: String, amount: Double) extends Transaction

  // 6) create a flow that just gets a list of strings and gets a Transaction for us
  // WARNING: This can fail if the string `amount` is not parseable to Double
  val parsed = Flow[List[String]] collect {
    case "Withdraw" :: name :: amount :: _ => Withdraw(name, amount.toDouble)
    case "Deposit" :: name :: amount :: _ => Deposit(name, amount.toDouble)
  }

  // Fourth test, lets parse everything!
  transactionSource.via(rows).via(parsed).runForeach(println)

  // ---- Let's apply our transactions and stop playing with a toy sink ----
  type Bank = Map[String, Double]
  // 7) Create a function that will be applied to each of the transactions parsed
  def applyTransaction(t: Transaction, bank: Bank): Bank = t match {
    case Deposit(name, amount) => bank.updated(name, bank.getOrElse(name, 0D) + amount)
    case Withdraw(name, amount) => bank.updated(name, bank.getOrElse(name, 0D) - amount)
  }

  val applyTransactionsSink: Sink[Transaction, Future[Bank]] =
    Sink.fold(Map.empty[String, Double])((bank, transaction) => applyTransaction(transaction, bank))

  // 8) Now we want to apply this Sink to our parsed transactions. To do that we need to materialize
  // all the blueprint to a future of the output, when the upstream completes
  val parseAndApplyTransactions: Sink[String, Future[Bank]] =
    rows.via(parsed).toMat(applyTransactionsSink)(Keep.right)

  val pipeline = transactionSource.toMat(parseAndApplyTransactions)(Keep.right)
  // This is all a description --> a blueprint, like a Spark sequence of transformations
  Await.result(pipeline.run(), 1 second)

  // So far so good, nothing that couldn't be done by simply using scala collections.
  // 9) Fun stuff begins, lets work with a generated csv of 1 million lines
  val bigCSVSource = Source.single("Type, Name, Amount").concat(Source.repeat("Deposit,Jane Doe, 1").take(1000000))


  // Materialized value: value when we run the pipeline. This proportionates the ability to run the pipeline many times
  // For example, a socket may resolve to a Closeable, that can be closed when the materialization finishes
  val pipelineBig = bigCSVSource.toMat(parseAndApplyTransactions)(Keep.right)
  Await.result(pipelineBig.run(), 1 second)

  // ---- END OF TRANSACTIONS ----

  val parseAndApplyWS =
    Flow[Message].collect {
      case TextMessage.Strict(txt) => txt
    }.via(allRowsFlow).via(parsed).
      scan(Map.empty[String, Double])((bank, transaction) => applyTransaction(transaction, bank)).
      map(bank => TextMessage.Strict(bank.toString))


//  object Deposit {
//    def unapply(toParse: List[String]): Option[(String, Double)] = toParse match {
//      case "Deposit" :: name :: amount :: _ => Some((name, amount.toDouble))
//      case _ => None
//    }
//  }
//
//  object Withdraw {
//    def unapply(toParse: List[String]): Option[(String, Double)] = toParse match {
//      case "Withdraw" :: name :: amount :: _ => Some((name, amount.toDouble))
//      case _ => None
//    }
//  }
//
//  val parsedWithPower = Flow[List[String]] collect {
//    case Deposit(name, amount) => Deposit(name, amount)
//    case Withdraw(name, amount) => Withdraw(name, amount)
//  }
}

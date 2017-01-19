package com.tomduhourq.hashtags

import scala.concurrent.Future

class ImplicitsSpec extends BaseSpec with ActorSystemGracefulShutdown {

  //Future pimping
  def simpleFuture = Future { println("Success!") }

  "My Future pimping" - {
    "should gracefully terminate an implicit actor system" in {
      system.whenTerminated.map(_ => { println("succeded"); succeed } )
      simpleFuture.gracefulShutdown
      succeed
    }
  }

  // Tweet enhancer
  val plainText = "Lightbend is looking for excellent people who like #Akka #Scala #Play #Lagom #Spark #Java to join our team!"
  val retweetText = "RT @lightbend: " + plainText

  "My Tweet enhancer" - {
    "removes the retweet header" in {
      retweetText.removeRetweet shouldEqual plainText
    }
    "keeps the same text if no retweet header is available" in {
      plainText.removeRetweet shouldEqual plainText
    }
  }
}

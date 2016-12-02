package com.tomduhourq

import akka.actor.ActorSystem

import scala.concurrent.{ExecutionContext, Future}

package object hashtags {

  val blockingTweetsPath = "target/interesting-tweets.txt"
  val streamingTweetsPath = "target/streaming-tweets.txt"

  type Tweet = String

  implicit class FuturePimping[T](val f: Future[T])(implicit ec: ExecutionContext) {
    def gracefulShutdown(implicit system: ActorSystem): Unit = f onComplete (_ => system.terminate())
  }

  implicit class TweetTextPimping(val s: Tweet) {
    def removeRetweet = if(s.startsWith("RT")) s.dropWhile(':'!=).drop(2) else s
    def containsAny(beginnings: String*) = beginnings.foldLeft(false)(_ || s.contains(_))
  }
}

package com.tomduhourq.hashtags.processing

import akka.stream.IOResult
import akka.stream.scaladsl.{Keep, Sink}
import com.tomduhourq.hashtags._
import com.tomduhourq.hashtags.configuration.{AkkaSystemConfiguration, FileHandler, Twitter}
import com.tomduhourq.hashtags.domain.InterestingTweet
import twitter4j.FilterQuery

import scala.concurrent.Future

object StreamingHashtags extends App with Twitter with FileHandler with AkkaSystemConfiguration {
  import CommonFlows._
  import CommonSinks._
  import system.dispatcher

  val interestingTweetsToFileSink: Sink[InterestingTweet, Future[IOResult]] =
    byteStringFlow[InterestingTweet](tweet => tweet.removeRetweet).
    toMat(writeToFileSink(streamingTweetsPath))(Keep.right)

  val query = new FilterQuery()
  query.track(InterestingHashtags:_*)

  StatusListener.source(query).
    filter(tweet => tweet.text.containsAny("http", "https")).
    runWith(interestingTweetsToFileSink).
    gracefulShutdown(system)
}

package com.tomduhourq.hashtags.processing

import akka.stream.ClosedShape
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, RunnableGraph, Source}
import com.tomduhourq.hashtags._
import com.tomduhourq.hashtags.configuration.{AkkaSystemConfiguration, FileHandler, Twitter}
import com.tomduhourq.hashtags.domain.InterestingTweet
import twitter4j.Query

import scala.collection.JavaConversions._

object BlockingHashtags extends App with Twitter with FileHandler with AkkaSystemConfiguration {
  import CommonFlows._
  import CommonSinks._
  import system.dispatcher

  val uniqueInterestingTweetsFlow =
    Flow[InterestingTweet].
      fold(Set.empty[InterestingTweet])((set, tweet) => if(set.exists(_.text == tweet.text)) set else set + tweet)

  val interestingTweetsToFileSink =
    byteStringFlow[InterestingTweet](tweet => tweet.removeRetweet).
    toMat(writeToFileSink(blockingTweetsPath))(Keep.right)

  def streamHashtagQuery(hashtag: String) = {
    val q = new Query(hashtag)
    q.count(100)

    Source.fromIterator(() => twitter.search(q).getTweets.iterator()).
      collect{
        case status if status.getRetweetCount > 10 => InterestingTweet(status.getRetweetCount, status.getFavoriteCount, status.getText.removeRetweet)
      }.
      via(uniqueInterestingTweetsFlow)
  }

  // Here we go
  val tweetSource = InterestingHashtags.
    map(streamHashtagQuery).
    reduce((acumSources, source) => acumSources.concat(source)).
    mapConcat(identity)

  val graph = GraphDSL.create(interestingTweetsToFileSink, consoleSink)(Keep.left) { implicit builder =>
    (file, console) =>
      import GraphDSL.Implicits._
      val broadcast = builder.add(Broadcast[InterestingTweet](2)) // the splitter - like a Unix tee

      tweetSource ~> broadcast ~> file
                     broadcast ~> console
      ClosedShape
  }

  val materialized = RunnableGraph.fromGraph(graph).run()

  materialized.gracefulShutdown
}

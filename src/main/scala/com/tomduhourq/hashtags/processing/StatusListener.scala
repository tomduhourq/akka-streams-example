package com.tomduhourq.hashtags.processing

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.tomduhourq.hashtags.configuration
import com.tomduhourq.hashtags.domain.InterestingTweet
import twitter4j._

object StatusListener extends configuration.Twitter {
  implicit val system = ActorSystem("Status-Listener")
  implicit val materializer = ActorMaterializer()

  /**
   * TODO: Graphics on tweets pipeline
   * Creates an actor that will receive Interesting Tweets as input and gets a publisher for us for a specific
   * filter query, which will be one per interesting hashtag
   *
   * @param query the filter query
   * @return a source of interesting tweet to plug in
   */
  def source(query: FilterQuery): Source[InterestingTweet, NotUsed] = {

    val (actorRef, publisher) = Source.actorRef[InterestingTweet](1000, OverflowStrategy.backpressure).toMat(Sink.asPublisher(true))(Keep.both).run()

    val statusListener = new StatusListener {
      override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = ()

      override def onException(ex: Exception): Unit = ()

      override def onStallWarning(warning: StallWarning): Unit = ()

      override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = ()

      override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = ()

      override def onStatus(status: Status): Unit = {
        actorRef ! InterestingTweet(status.getRetweetCount, status.getFavoriteCount, status.getText)
      }
    }

    twitterStreaming.addListener(statusListener)
    twitterStreaming.filter(query)

    Source.fromPublisher(publisher)
  }
}

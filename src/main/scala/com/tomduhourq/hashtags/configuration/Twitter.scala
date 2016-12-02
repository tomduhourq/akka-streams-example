package com.tomduhourq.hashtags.configuration

import twitter4j.conf.ConfigurationBuilder
import twitter4j.{TwitterFactory, TwitterStreamFactory}

trait Twitter {
  val cb = new ConfigurationBuilder()

  cb.setDebugEnabled(true)
    .setOAuthConsumerKey("xxxxxxx")
    .setOAuthConsumerSecret("xxxxxxx")
    .setOAuthAccessToken("xxxxxxx")
    .setOAuthAccessTokenSecret("xxxxxxx")

  private val twitterConfBuilder = cb.build()
  val twitter = new TwitterFactory(twitterConfBuilder).getInstance()
  val twitterStreaming = new TwitterStreamFactory(twitterConfBuilder).getInstance
}

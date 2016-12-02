package com.tomduhourq.hashtags.configuration

import twitter4j.conf.ConfigurationBuilder
import twitter4j.{TwitterFactory, TwitterStreamFactory}

trait Twitter {
  val cb = new ConfigurationBuilder()

  cb.setDebugEnabled(true)
    .setOAuthConsumerKey("d9DZgRHpYc0SeWR2tNnMi2RVj")
    .setOAuthConsumerSecret("XBrntcAm1mMK14ymVJEAEfgb01ThYVh6oYVQl8uPo9Wb9e6t8q")
    .setOAuthAccessToken("2199571344-t5dqEdAX60Konm7A85iEK8xHoc5YSoSZg4uOgFK")
    .setOAuthAccessTokenSecret("5oPE7dAOTvopcrmsy0BKAIkiYfyxUvVazzK0oJYtLplqr")

  private val twitterConfBuilder = cb.build()
  val twitter = new TwitterFactory(twitterConfBuilder).getInstance()
  val twitterStreaming = new TwitterStreamFactory(twitterConfBuilder).getInstance
}

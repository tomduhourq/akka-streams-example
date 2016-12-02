package com.tomduhourq.hashtags.domain

import com.tomduhourq.hashtags.TweetTextPimping

final case class InterestingTweet(retweets: Int, likes: Int, text: String) {
  val removeRetweet = text.removeRetweet
}

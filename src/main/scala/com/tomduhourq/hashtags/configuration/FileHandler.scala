package com.tomduhourq.hashtags.configuration

trait FileHandler {
  private val hashtagsFile = io.Source.fromInputStream(getClass.getResourceAsStream("/interesting-hashtags.txt"))
  val InterestingHashtags = try hashtagsFile.getLines().toList.map(word => s"#$word") finally hashtagsFile.close()
}
package com.tomduhourq.hashtags.processing

import java.io.{File, PrintWriter}

import akka.stream.IOResult
import akka.stream.scaladsl.{Keep, Sink, Source}
import com.tomduhourq.hashtags.domain.InterestingTweet
import com.tomduhourq.hashtags.processing.CommonSources._
import com.tomduhourq.hashtags.processing.CommonFlows._
import com.tomduhourq.hashtags.processing.CommonSinks._
import com.tomduhourq.hashtags.{ActorSystemTesting, BaseSpec}

import scala.concurrent.Future

class CommonStreamsSpec extends BaseSpec with ActorSystemTesting {

  val readFromFileDirectory = "src/test/resources/common/sources/read-test.txt"
  val readScalaSource = io.Source.fromInputStream(getClass.getResourceAsStream("/common/sources/read-test.txt"))
  val fileContentRead = readScalaSource.getLines

  val writeToFileDirectory = "src/test/resources/common/sinks/write-test.txt"
  val writeScalaSource = io.Source.fromInputStream(getClass.getResourceAsStream("/common/sinks/write-test.txt"))
  def fileContentWritten = writeScalaSource.getLines

  override def beforeAll = {
    // At the start, delete content on write file
    val writer = new PrintWriter(new File(writeToFileDirectory))
    writer.write("")
    writer.close()
  }

  "Common Sources" - {
    "Reading from path Source" - {
      "should read the content correctly if the path is on scope" in {
        val readIO: Future[String] =
          readFromPathSource(readFromFileDirectory).
            map(_.utf8String).
            runWith(Sink.head)

        // ByteString introduces a \n at the end, so taking it out for test purposes
        readIO.map(content => content.dropRight(1) shouldEqual fileContentRead.mkString("\n"))
      }
    }
  }

  "Common Flows" - {
    "Converting to ByteString" - {
      "should convert elements correctly to ByteStrings" in {
        val tweet = InterestingTweet(10, 0, "Awesome tweet!")
        val tweetStringRepresentation = (tweet: InterestingTweet) => tweet.text
        val byteStringFut = Source.single(tweet).via(byteStringFlow(tweetStringRepresentation)).runWith(Sink.head)

        byteStringFut.map(_.utf8String.dropRight(1) shouldEqual tweetStringRepresentation(tweet))
      }
    }
  }

  "Common Sinks" - {
    "Writing to path using a Sink" - {
      "should write to a specific file" in {
        val contentToWrite = "Testing a custom sink can be as simple as attaching a source that emits elements from a predefined collection, running a constructed test flow and asserting on the results that sink produced."
        val writeIO: Future[IOResult] =
          Source.
            single(contentToWrite).
            via(byteStringFlow(identity)).
            runWith(writeToFileSink(writeToFileDirectory))

        writeIO.map(_ => fileContentWritten.mkString("") shouldEqual contentToWrite)
      }
    }
  }

  override def afterAll = {
    readScalaSource.close
    writeScalaSource.close
    super.afterAll
  }
}

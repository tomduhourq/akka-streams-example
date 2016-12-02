package com.tomduhourq.hashtags

import java.nio.file.Paths

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.util.ByteString
import akka.{Done, NotUsed}

import scala.concurrent.Future

/**
 * Container of several Source, Flow and Sinks for processing
 */
package object processing {

  object CommonSources {

    /**
     * Source that reads from a target directory,
     * filling In with Akka `ByteString`
     *
     * @param path the target path to suck the data from
     * @return a Source of `ByteString`
     */
    def readFromPathSource(path: String): Source[ByteString, Future[IOResult]] = FileIO.fromPath(Paths.get(path))
  }

  object CommonFlows {

    /**
     * Flow that takes any A and a string representation function
     * and converts each element to an Akka `ByteString`
     *
     * @param stringRep the string representation function
     * @tparam A a generic type
     * @return a Flow that takes As and pipes them to `ByteString`
     */
    def byteStringFlow[A](stringRep: A => String): Flow[A, ByteString, NotUsed] =
      Flow[A].map(a => ByteString(s"${stringRep(a)}\n"))
  }

  object CommonSinks {

    /**
     * Sink that logs to console
     */
    def consoleSink[A]: Sink[A, Future[Done]] = Sink.foreach[A](println)

    /**
     * Sink that writes data to the given path
     *
     * @param path the path to write the file to
     * @return a Sink that writes Akka `ByteString` to the given path
     */
    def writeToFileSink(path: String): Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get(path))
  }
}

package com.tomduhourq.hashtags.examples

import java.nio.file.Paths

import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import com.tomduhourq.hashtags.configuration.AkkaSystemConfiguration

object Factorials extends AkkaSystemConfiguration with App {
  import system.dispatcher

  val source = Source(1 to 1000)

  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)

  val result = factorials.
    map(num => {println(s"Going through $num"); ByteString(s"$num\n")}).
    runWith(FileIO.toPath(Paths.get("factorials.txt")))

  result onComplete (_ => system.terminate())
}

package com.tomduhourq.hashtags

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.{BeforeAndAfterAll, Suite}

trait ActorSystemTesting extends BeforeAndAfterAll { self: Suite =>
  implicit val system = ActorSystem("test-system")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  override def afterAll: Unit = {
    materializer.shutdown()
    system.terminate()
  }
}

package com.tomduhourq.hashtags.configuration

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

trait AkkaSystemConfiguration {
  implicit val system = ActorSystem("Sys")
  implicit val materializer = ActorMaterializer.create(system)
}

import Dependencies.fullDependencies

val projectName = "scala-hashtags-akka-streams"

name := projectName

scalaVersion := "2.11.8"

version := "1.0"

libraryDependencies ++= fullDependencies

assemblyJarName in assembly := projectName + ".jar"

mainClass in assembly := Some("com.tomduhourq.hashtags.http.Server")

initialCommands in console :=
  s"""
   import akka.actor.ActorSystem
   import akka.stream.ActorMaterializer
   import akka.stream.scaladsl.{FileIO, Flow, Sink, Source, RunnableGraph, Keep}
   import scala.concurrent.{Future, Await}
   import scala.concurrent.duration._

   implicit val system = ActorSystem("console-system")
   import system.dispatcher
   implicit val materializer = ActorMaterializer()
  """

lazy val root = (project in file(".")).
  enablePlugins(DockerPlugin).
  settings(
    dockerfile in docker := {
      val artifact: File = assembly.value
      val artifactTargetPath = s"/app/${artifact.name}"
      Docker.dockerFile(artifact, artifactTargetPath)
    },
    imageNames in docker := Seq(
      ImageName(s"tomduhourq/$projectName:latest")
    )
  )

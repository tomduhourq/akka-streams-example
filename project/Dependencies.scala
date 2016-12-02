import sbt._

object Dependencies {

  trait DependenciesHolder{
    def dependencies: List[ModuleID]
  }

  val akkaVersion = "2.4.12"

  object Core extends DependenciesHolder {
    val akkaHttpVersion = "2.4.11"
    val twitter4jVersion = "4.0.5"

    val akkaStreams = "com.typesafe.akka" %% "akka-stream"            % akkaVersion
    val akkaHttp    = "com.typesafe.akka" %% "akka-http-experimental" % akkaHttpVersion
    val twitter4j   = "org.twitter4j"      % "twitter4j-stream"       % twitter4jVersion

    def dependencies = List(akkaStreams, akkaHttp, twitter4j)
  }

  object Test extends DependenciesHolder {
    val scalatestVersion = "3.0.0"

    val akkaStreamTestKit = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
    val scalactic         = "org.scalactic"     %% "scalactic"           % scalatestVersion
    val scalatest         = "org.scalatest"     %% "scalatest"           % scalatestVersion % "test"
    
    def dependencies = List(akkaStreamTestKit, scalactic, scalatest)
  }

  val modules = List(Core, Test)

  val fullDependencies: List[ModuleID] = modules.map(_.dependencies).reduce(_ ++ _)
}
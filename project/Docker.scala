import sbt.File
import sbtdocker.mutable.Dockerfile

object Docker {
  def dockerFile(artifact: File, artifactTargetPath: String) = new Dockerfile {
    from("java")
    add(artifact, artifactTargetPath)
    expose(8080)
    cmdRaw(s"java -jar $artifactTargetPath com.tomduhourq.hashtags.processing.BlockingHashtags")
    cmdRaw(s"java -jar $artifactTargetPath com.tomduhourq.hashtags.http.Server")
    cmdRaw(s"java -jar $artifactTargetPath com.tomduhourq.hashtags.processing.StreamingHashtags")
  }
}
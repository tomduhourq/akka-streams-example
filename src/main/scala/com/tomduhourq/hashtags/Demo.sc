import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import com.tomduhourq.hashtags.domain.InterestingTweet

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

implicit val system = ActorSystem("console-system")
implicit val materializer = ActorMaterializer()

val source: Source[Int, NotUsed] = Source(1 to 10)

val sink = Sink.fold[Int, Int](0)(_ + _)

val runnable: RunnableGraph[Future[Int]] = source.toMat(sink)(Keep.right)

val sum = runnable.run()
val sum2 = source.runWith(sink)

Await.result(runnable.run(), 2 seconds) // 55

Source(List(1, 2, 3))

// Create a source from a Future
Source.fromFuture(Future.successful("Hello Streams!"))

// Create a source from a single element
Source.single("only one element")

// an empty source
Source.empty

// Sink that folds over the stream and returns a Future
// of the final result as its materialized value
Sink.fold[Int, Int](0)(_ + _)

// Sink that returns a Future as its materialized value,
// containing the first element of the stream
Sink.head

// A Sink that consumes a stream without doing anything with the elements
Sink.ignore

// A Sink that executes a side-effecting call for every element of the stream
Sink.foreach[String](println(_))

val flow = Flow[Int].map(_ * 2).filter(_ > 500)

Flow[InterestingTweet].
  fold(Set.empty[InterestingTweet])((set, tweet) => if(set.exists(_.text == tweet.text)) set else set + tweet)

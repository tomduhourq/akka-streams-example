# scala-hashtags-akka-streams

A personal example to learn Akka Streams basics and Docker

This includes:

- Basic example on bank accounts and web sockets taken from [Paul Kinsky's intro to akka-streams](https://www.youtube.com/watch?v=WlL6ibfPfgg&t=1181s)
- Blocking Twitter consumption to show Graph DSL
- Streaming Twitter Publisher Actor pipeline on to a file
- Akka Http streaming endpoints that consume the files from both processes
- Tests - I couldn't get to use the akka-stream-testkit yet

Usage:

```bash
$ sbt run
```

will bring up a couple of main classes to run:

- you might want to choose the `Server.scala` to make both endpoints (tweets and websockets) available
- `StreamingHashtags.scala` to boot up a streaming processing of tweets on to a file on your `/target` directory
- `BlockingHashtags.scala` will output both to console and to a file named `interesting-tweets.txt` the most recent tweets
that interested me according to the file on `src/resources/interesting-hashtags.txt`

```bash
$ sbt test
```

will output some tests results, they test most of the machinery behind these examples and common `Source`s, `Flow`s and `Sink`s.

PS: Don't forget to fill in your twitter creds in order to access the Twitter API. This is on the `Twitter.scala` file.

##API

- `/` will pop up the bank accounts backendish frontend, if you fill in a name and an amount and click on an action you should be able
to see the bank's current state if you inspect what's going on on the browser's console
- `/ws` this endpoint is used by the bank accounts frontend to send the `Withdraw` or `Deposit` action through the websocket
- `/tweets/blocking` will bring up the contents of a local file in your `target` folder. You need to run `BlockingHashtags.scala` before.
- `/tweets/streaming` will bring up the contents of a local file in your `target` folder, product of running `StreamingHashtags.scala`

##Docker

I used the awesome [sbt-docker](https://github.com/marcuslonnberg/sbt-docker) plugin to push this project and run it as a container
anywhere, free of installing Scala or sbt.
Just remember to configure where to push it on the `imageName` field on `build.sbt`

```bash
$ sbt dockerBuildAndPush
```
should do the trick for you.

##Contributing

Feel obviously free to contribute to this project, open a pull request, or an issue and I'll be happy to address it. I've just
started out with this awesome tool, constructive criticism is accepted.
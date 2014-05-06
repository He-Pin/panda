name := "PandaFramework"

organization:= "us.sosia"

version := "1.0"

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

libraryDependencies ++= Seq(
  "io.netty" % "netty-all" % "4.0.18.Final",//for network
  "com.typesafe.akka" % "akka-actor_2.10" % "2.3.2",//for event processing
  "com.fasterxml.jackson.core" % "jackson-core" % "2.3.2",//for json
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.3.2",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.3.2"
)

name := "PandaFramework"

organization:= "panda"

version := "0.0.1-SNAPSHOT"

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

libraryDependencies ++= Seq(
  "io.netty" % "netty-all" % "4.0.19.Final",//for network
  "com.typesafe.akka" % "akka-actor_2.11" % "2.3.2",//for event processing
  "com.fasterxml.jackson.core" % "jackson-core" % "2.3.2",//for json
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.3.2",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.3.2"
)

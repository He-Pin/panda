name := "proxy"

version := "1.0"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "io.netty" % "netty-all" % "4.0.21.Final",//for network
  "com.typesafe.akka" % "akka-actor_2.11" % "2.3.4",//for event processing
  //"com.typesafe.akka" % "akka-remote_2.11" % "2.3.4",
  "com.barchart.udt" % "barchart-udt-bundle" % "2.3.0",
  "com.google.protobuf" % "protobuf-java" % "2.5.0",
  "com.ning" % "async-http-client" % "1.8.12"
)

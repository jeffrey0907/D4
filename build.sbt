name := s"""D4"""

organization := "D4"

scalaVersion := "2.10.4"


libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.10" % "2.2.3",
  "com.typesafe.akka" % "akka-actor_2.10" % "2.3.5",
  "com.typesafe.akka" % "akka-contrib_2.10" % "2.3.5",
  "com.typesafe.akka" % "akka-cluster_2.10" % "2.3.5",
  "com.typesafe.akka" % "akka-persistence-experimental_2.10" % "2.3.5",
  "org.slf4j" % "slf4j-api" % "1.7.10",
  "org.slf4j" % "slf4j-simple" % "1.7.10"
)

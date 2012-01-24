name := "omniture-api-client"

organization := "com.gu"

scalaVersion := "2.9.1"

publishArtifact := true

version in ThisBuild := "0.0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-http" % "0.8.5",
  "commons-codec" % "commons-codec" % "1.6",
  "net.liftweb" %% "lift-json" % "2.4-M4",
  "org.scalatest" %% "scalatest" % "1.6.1" % "test"
)

scalacOptions ++= Seq("-unchecked", "-deprecation")
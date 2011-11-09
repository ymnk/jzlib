// -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*-

name := "jzlib"

organization := "JCraft,Inc."

version      := "1.1.0"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.6.1" % "test",
  "junit" % "junit" % "4.10" % "test->default",
  "com.novocode" % "junit-interface" % "0.7"% "test"
)

javacOptions ++= Seq(
  "-target", "1.5"
)

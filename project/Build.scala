import sbt._
import Keys._

object BuildSettings {
  val buildName = "jzlib"
  val buildOrganization = "JCraft,Inc."
  val buildVersion = "1.1.3"
  val buildScalaVersion = "2.10.1"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    crossPaths   := false,
    javaOptions ++= Seq (
      "-target", "1.5"
    )
  )
}

object MyBuild extends Build {

  import BuildSettings._

  val scalatest = "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"

  private val dependencies = Seq (
    scalatest
  )

  lazy val root =
    Project( buildName, file("."), settings = buildSettings )
      .settings( libraryDependencies ++= dependencies )
      .settings( parallelExecution in Test := false )
}

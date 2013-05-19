import sbt._
import Keys._

object BuildSettings {
  val buildName = "jzlib"
  val buildOrganization = "JCraft,Inc."
  val buildVersion = "1.1.2"
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

  private val test_argument = 
    Tests.Argument( TestFrameworks.JUnit, "-q", "-v" )

  lazy val root =
    Project( buildName, file("."), settings = buildSettings )
      .settings( libraryDependencies ++= dependencies )
      .settings( testOptions in Test += test_argument )
      .settings( parallelExecution in Test := false )
}

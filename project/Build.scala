import sbt._
import Keys._

object BuildSettings {
  val buildName = "jzlib"
  val buildOrganization = "JCraft,Inc."
  val buildVersion = "1.1.1"
  val buildScalaVersion = "2.9.1"

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

  val scalatest = "org.scalatest" %% "scalatest" % "1.6.1"
  val junit = "junit" % "junit" % "4.8"
  val junit_interface = "com.novocode" % "junit-interface" % "0.7"

  private val dependencies = Seq (
    scalatest, junit, junit_interface
  )

  private val test_argument = 
    Tests.Argument( TestFrameworks.JUnit, "-q", "-v" )

  lazy val root =
    Project( buildName, file("."), settings = buildSettings )
      .settings( libraryDependencies ++= dependencies )
      .settings( testOptions in Test += test_argument )
      .settings( parallelExecution in Test := false )
}

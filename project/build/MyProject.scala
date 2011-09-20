import sbt._

class MyProject(info: ProjectInfo) extends DefaultProject(info) {
  val scalatest = "org.scalatest" %% "scalatest" % "1.6.1"
  val junit4 = "junit" % "junit" % "4.8"
  val junitInterface = "com.novocode" % "junit-interface" % "0.7"

  override def artifactID = "jzlib"

  override def compileOptions =
     super.compileOptions ++compileOptions("-target:jvm-1.5") 

  override def testOptions =
    super.testOptions ++
    Seq(TestArgument(TestFrameworks.JUnit, "-q", "-v"))
}

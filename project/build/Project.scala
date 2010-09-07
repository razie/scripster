import sbt._

class PScripster(info: ProjectInfo) extends DefaultProject(info) {
  val scalatest = "org.scalatest" % "scalatest" % "1.2"
  val scalaSwing = "org.scala-lang" % "scala-swing" % "2.8.0"

  val razBase = "razie.pub" %% "razbase"         % "0.1"
  val w20     = "razie.pub" %% "20widgets"       % "1.0"
  val w20s    = "razie.pub" %% "20widgets-swing" % "1.0"
  val razWeb  = "razie.pub" %% "razweb"          % "1.0"

  override def unmanagedClasspath = 
    super.unmanagedClasspath +++ 
      (Path.fromFile ("../razbase/lib") / "json.jar")
 
  override def mainScalaSourcePath = "src"
  override def testScalaSourcePath = "test_src"
}


import sbt._

class PScripster(info: ProjectInfo) extends DefaultProject(info) {
  val SCALAVER = "2.8.1.RC1"
    
  val scalatest  = "org.scalatest" % "scalatest" % "1.2"
  val scalaSwing = "org.scala-lang" % "scala-swing" % SCALAVER
  val scalaComp  = "org.scala-lang" % "scala-compiler" % SCALAVER % "test->default"
  val scalaLib   = "org.scala-lang" % "scala-library" % SCALAVER % "test->default"
  val junit      = "junit" % "junit" % "4.5" % "test->default"

  val scalazCore = "com.googlecode.scalaz" % "scalaz-core_2.8.0" % "5.0"

  val razBase = "com.razie" %% "razbase"         % "0.1-SNAPSHOT"
  val w20     = "com.razie" %% "20widgets"       % "0.1-SNAPSHOT"
  val w20s    = "com.razie" %% "20widgets-swing" % "0.1-SNAPSHOT"
  val razWeb  = "com.razie" %% "razweb"          % "0.1-SNAPSHOT"
  

  override def unmanagedClasspath = 
    super.unmanagedClasspath +++ 
      (Path.fromFile ("../razbase/lib") / "json.jar")
 
//  override def mainScalaSourcePath = "src"
//  override def mainResourcesPath = "src"
  override def testScalaSourcePath = "test_src"
}


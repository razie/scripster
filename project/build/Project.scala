import sbt._

// TODO repackaging, see http://github.com/jboner/akka/blob/master/project/build/AkkaProject.scala
class Project(info: ProjectInfo) extends DefaultProject(info) {
  val SCALAVER = "2.8.1.RC1"
  val RAZBASEVER = "0.1-SNAPSHOT"
    
  val scalatest  = "org.scalatest" % "scalatest" % "1.2"
  val scalaSwing = "org.scala-lang" % "scala-swing" % SCALAVER
  val scalaComp  = "org.scala-lang" % "scala-compiler" % SCALAVER % "test->default"
  val scalaLib   = "org.scala-lang" % "scala-library" % SCALAVER % "test->default"
  val junit      = "junit" % "junit" % "4.5" % "test->default"

  val scalazCore = "com.googlecode.scalaz" % "scalaz-core_2.8.0" % "5.0"

  val razBase = "com.razie" %% "razbase"         % RAZBASEVER

  override def unmanagedClasspath = 
    super.unmanagedClasspath +++ 
      (Path.fromFile ("../razbase/lib") / "json.jar")
}


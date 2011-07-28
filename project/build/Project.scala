import sbt._

// TODO repackaging, see http://github.com/jboner/akka/blob/master/project/build/AkkaProject.scala

class Project(info: ProjectInfo) extends DefaultProject(info) with posterous.Publish {

  override def managedStyle = ManagedStyle.Maven
  //val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
  Credentials(Path.userHome / ".ivy2.credentials", log)
      
  val SCALAVER = "2.9.0-1"
  //val RAZBASEVER = "0.3-SNAPSHOT"
  val RAZBASEVER = "0.3"
//  val bootcp     = "com.razie" % "scripster-bootcp_2.8.1" % "2.8.1" //% "test->default"
    
  val scalatest = "org.scalatest" % "scalatest_2.9.0" % "1.6.1"
  val scalaSwing = "org.scala-lang" % "scala-swing" % SCALAVER
  val scalaComp  = "org.scala-lang" % "scala-compiler" % SCALAVER % "test->default"
  val scalaLib   = "org.scala-lang" % "scala-library" % SCALAVER % "test->default"
  val junit      = "junit" % "junit" % "4.5" % "test->default"
  val json       = "org.json" % "json" % "20090211"

  val scalazCore = "org.scalaz" % "scalaz-core_2.9.0-1" % "6.0.1"

  val razBase = "com.razie" %% "razbase"         % RAZBASEVER
}


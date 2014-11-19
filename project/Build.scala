import sbt._
import Keys._

object V {
  val version      = "0.8.6-SNAPSHOT"
  val scalaVersion = "2.10.4" 
  val organization = "com.razie"

  def snap = (if (V.version endsWith "-SNAPSHOT") "-SNAPSHOT" else "")

  def SCALAVER   = scalaVersion

  def RAZBASEVER = "0.6.6" + snap
  def SNAKKVER   = "0.6.6" + snap
  def LIGHTSOAVER = "0.6.6" + snap
}

object MyBuild extends Build {

  def scalatest = "org.scalatest" %% "scalatest"       % "1.9.2"
  def junit     = "junit"          % "junit"           % "4.5"      % "test->default"
  def json      = "org.json"       % "json"            % "20090211"
  
  def scalaSwing = "org.scala-lang" % "scala-swing"    % V.SCALAVER
  def scalaComp  = "org.scala-lang" % "scala-compiler" % V.SCALAVER 
  def scalaLib   = "org.scala-lang" % "scala-library"  % V.SCALAVER 

  def scalazCore = "org.scalaz"    %% "scalaz-core"    % "7.0.3"

  val snakk    = "com.razie" %% "snakk-core"      % V.SNAKKVER
  def razBase  = "com.razie" %% "base"            % V.RAZBASEVER
  def razBaseA = "com.razie" %% "razbase"         % V.RAZBASEVER
  def swing20  = "com.razie" %% "s20swing"        % V.RAZBASEVER
  def lightsoa = "com.razie" %% "lightsoa-core"   % V.LIGHTSOAVER

  lazy val root = Project(id="scripster",    base=file("."),
    settings = defaultSettings ++ Seq()
    ) aggregate (pcore, pweb)

  lazy val pcore = Project(id="scripster-core",    base=file("core"),
    settings = defaultSettings ++ 
      Seq(libraryDependencies ++= Seq(
        scalatest, junit, json, scalaComp, razBase, snakk))
        ) 

  lazy val pweb = Project(id="scripster-web",    base=file("web"),
    settings = defaultSettings ++ 
      Seq(libraryDependencies ++= Seq(
        scalatest, junit, json, scalaSwing, scalaComp, scalazCore, razBaseA, snakk, lightsoa, swing20))
        ) dependsOn (pcore)

  def defaultSettings = baseSettings ++ Seq()
  def baseSettings = Defaults.defaultSettings ++ Seq (
    scalaVersion         := V.scalaVersion,
    version              := V.version,

    organization         := V.organization,
    organizationName     := "Razie's Pub",
    organizationHomepage := Some(url("http://www.razie.com")),

    publishTo <<= version { (v: String) =>
      if(v endsWith "-SNAPSHOT")
        Some ("Sonatype" at "https://oss.sonatype.org/content/repositories/snapshots/")
      else
        Some ("Sonatype" at "https://oss.sonatype.org/content/repositories/releases/")
    } ,

    resolvers ++= Seq("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
                      "releases"  at "https://oss.sonatype.org/content/repositories/releases")
    )
}


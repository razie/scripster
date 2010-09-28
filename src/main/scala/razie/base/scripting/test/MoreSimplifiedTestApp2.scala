package razie.base.scripting.test;

import scala.tools.{nsc => nsc}

object MoreSimplifiedTestApp2 extends Application {
  val settings = {
     val set = new nsc.Settings()
     set.classpath.value += java.io.File.pathSeparator + System.getProperty ("java.class.path")
     set
  }
  val in = new nsc.Interpreter () {
     override protected def parentClassLoader = MoreSimplifiedTestApp2.getClass.getClassLoader
  }
  in.setContextClassLoader
  println (in interpret ("1+2"))
}

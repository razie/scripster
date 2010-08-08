package razie.base.scripting.test;

import scala.tools.{nsc => nsc}

object MoreSimplifiedTestApp extends Application {
  println (new nsc.Interpreter() interpret ("1+2"))
}

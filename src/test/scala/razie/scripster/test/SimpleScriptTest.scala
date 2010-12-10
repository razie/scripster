package razie.scripster.test

import org.scalatest.junit._
import razie.base.scripting._
import razie.scripster._

class SimpleScriptTest extends JUnit3Suite {

  def xtestsimple1 = expect (3) {
    // simple, one time, expression
    ScalaScript ("1+2").eval (ScriptContextImpl.global) getOrElse "?"
  }

  def testshare = expect ("12") {
    // test sharing variables - this is possible because populated variables end up in the context and we 
    // share the context
    val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
    ScalaScript ("val c = a+b").interactive (ctx)
    ScalaScript ("c").interactive (ctx) getOrElse "?"
  }

}

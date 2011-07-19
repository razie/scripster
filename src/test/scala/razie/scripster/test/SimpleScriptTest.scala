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

    // simple, one time, expression
  def testexpr2 = expect (3) { 
    ScalaScript("1+2").eval (ScalaScriptContext(Map())) getOrElse "?"
    }

  // test binding variables
  def testbinding = expect ("12") {
    ScalaScript ("a+b").eval (ScalaScriptContext(Map("a" -> "1", "b" -> "2"))) getOrElse "?"
    }

  // test sharing variables - this is possible because populated variables end up in the context and we 
  // share the context
  def testsharing = expect ("12") {
     val ctx = ScalaScriptContext(Map("a" -> "1", "b" -> "2"))
     ScalaScript("val c = a+b").interactive (ctx)
     ScalaScript("c").interactive (ctx) getOrElse "?"
     }

  // options
  def testoptions = expect (true) {
     val ctx = ScalaScriptContext(Map("a" -> 1, "b" -> 2))
     ctx.options ("java.lang.Sys") contains ("System")
     }

  // export new variables back into context
  def testexport = expect ("12") {
     val ctx = ScalaScriptContext(Map("a" -> "1", "b" -> "2")) 
     ScalaScript("val c = a+b").interactive (ctx)
     ctx getOrElse ("c", "?")
     }

  // test sharing defs
  def testsharingdef = expect (9) {
     val ctx = ScalaScriptContext(Map("a" -> 1, "b" -> 2))
     ScalaScript("""def f(x: Int) = x*x""").interactive (ctx)
     ScalaScript("""f (1+2)""").interactive (ctx) getOrElse "?"
     }

  // TOOD why can't i bind integers?
  def testbindint = expect (3) {
     val ctx = ScalaScriptContext(Map("a" -> 1, "b" -> 2))
     ScalaScript("val c = a+b").interactive (ctx)
     ctx getOrElse ("c", "?")
     }

}

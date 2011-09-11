package razie.scripster.test

import org.junit.Test
import razie.base.scripting._
import razie.scripster._
import org.scalatest.matchers.MustMatchers

class SimpleScriptTest extends MustMatchers {

  def xtestsimple1 = expect(3) {
    // simple, one time, expression
    ScalaScript("1+2").eval(ScriptContextImpl.global) getOrElse "?"
  }

  @Test def testshare = expect("12") {
    // test sharing variables - this is possible because populated variables end up in the context and we 
    // share the context
    val ctx = ScalaScriptContext("a" -> "1", "b" -> "2")
    ScalaScript("val c = a+b").interactive(ctx)
    ScalaScript("c").interactive(ctx) getOrElse "?"
  }

  // simple, one time, expression
  @Test def testexpr2 = expect(3) {
    ScalaScript("1+2").eval(ScalaScriptContext()) getOrElse "?"
  }

  // test binding variables
  @Test def testbinding = expect("12") {
    ScalaScript("a+b").eval(ScalaScriptContext("a" -> "1", "b" -> "2")) getOrElse "?"
  }

  // test sharing variables - this is possible because populated variables end up in the context and we 
  // share the context
  @Test def testsharing = expect("12") {
    val ctx = ScalaScriptContext("a" -> "1", "b" -> "2")
    ScalaScript("val c = a+b").interactive(ctx)
    ScalaScript("c").interactive(ctx) getOrElse "?"
  }

  // options
  @Test def testoptions = expect(true) {
    val ctx = ScalaScriptContext("a" -> 1, "b" -> 2)
    ctx.options("java.lang.Sys") contains ("System")
  }

  // export new variables back into context
  @Test def testexport = expect("12") {
    val ctx = ScalaScriptContext("a" -> "1", "b" -> "2")
    ScalaScript("val c = a+b").interactive(ctx)
    ctx getOrElse ("c", "?")
  }

  // test sharing defs
  @Test def testsharingdef = expect(9) {
    val ctx = ScalaScriptContext("a" -> 1, "b" -> 2)
    ScalaScript("""def f(x: Int) = x*x""").interactive(ctx)
    ScalaScript("""f (1+2)""").interactive(ctx) getOrElse "?"
  }

  // TOOD why can't i bind integers?
  @Test def testbindint = expect(3) {
    val ctx = ScalaScriptContext("a" -> 1, "b" -> 2)
    ScalaScript("val c = a+b").interactive(ctx)
    ctx getOrElse ("c", "?")
  }

  @Test def testerror1 = expect(true) {
    val ctx = ScalaScriptContext("a" -> 1, "b" -> 2)
    val res = ScalaScript("val c = a+-+b").interactive(ctx) match {
      case RazScript.RSError(msg) => msg
      case _ => ""
    }
    println(res)
    res contains "error"
  }

  @Test def testerror2 = expect(true) {
    val ctx = ScalaScriptContext("a" -> 1, "b" -> 2)
    val res = ScalaScript("val c = a+-+b").eval(ctx) match {
      case RazScript.RSError(msg) => msg
      case _ => ""
    }
    println(res)
    res contains "error"
  }

}

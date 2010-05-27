package razie.scripster.test

import org.scalatest.junit._
import razie.base.scripting._

class ScalaScriptTest extends JUnit3Suite {

  def testsimple1 = expect (3) { 
    // simple, one time, expression
    ScriptScala ("1+2").eval (ScriptContextImpl.global) getOrElse "?"
    }
  
  def testbind = expect ("12") { 
    // test binding variables
    ScriptScala ("a+b").eval (new ScalaScriptContext(null, "a", "1", "b", "2")) getOrElse "?"
    }

  def testshare = expect ("12") { 
     // test sharing variables - this is possible because populated variables end up in the context and we 
     // share the context
     val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
     ScriptScala ("val c = a+b").interactive (ctx) 
     ScriptScala ("c").interactive (ctx) getOrElse "?"
     }

  // export new variables back into context
  def testexport = expect ("12") {
     val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
     ScriptScala ("val c = a+b").interactive (ctx)
     ctx sa "c"
     }

  // options
  def testoptions = expect (true) {
     val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
     ctx.options ("java.lang.Sys") contains ("System")
     }
}

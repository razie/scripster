package razie.scripster.test

import org.scalatest.junit._
import razie.base.scripting._
import razie.scripster._

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
  
  def testdef = expect (5) { 
    // simple, one time, expression
     val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
    ScriptScala ("""
def add (i:Int*) = {
  val ss = "12344"
  var ii = 0
  for (k <- i) ii = ii + k
  ii
  }
"""
).interactive (ctx) 

     ScriptScala ("add (1+2, 8/4)").interactive (ctx) getOrElse "?" 
   }

  def testScripster = expect (3) {
     import razie.scripster._
     val c = Sessions.create (Scripster.sharedContext , "scala")
     Scripster.execWithin (10000) ("scala", "1+2", c.id)._2
  }
  
  def testTimeout = expect (true) {
     import razie.scripster._
     val c = Sessions.create (Scripster.sharedContext , "scala")
     razie.Timer {
        try {
        Scripster.execWithin (5000) ("scala", "while(true) print(\"\")", c.id)
        } catch { case _ => ; }
     } ._1 < 6000
  }
}

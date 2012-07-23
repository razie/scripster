package razie.scripster.test

import org.junit.Test
import razie.base.scripting._
import razie.scripster._
import org.scalatest.matchers.MustMatchers

class ScalaScriptTest extends MustMatchers {

  @Test def testsimple1 = expect (3) {
    // simple, one time, expression
    ScalaScript ("1+2").eval (ScriptContextImpl.global) getOrElse "?"
  }

  @Test def testbind = expect ("12") {
    // test binding variables
    ScalaScript ("a+b").eval (new ScalaScriptContext(null, "a", "1", "b", "2")) getOrElse "?"
  }

  @Test def testshare = expect ("12") {
    // test sharing variables - this is possible because populated variables end up in the context and we 
    // share the context
    val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
    ScalaScript ("val c = a+b").interactive (ctx)
    ScalaScript ("c").interactive (ctx) getOrElse "?"
  }

  // export new variables back into context
  @Test def testexport = expect ("12") {
    val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
    ScalaScript ("val c = a+b").interactive (ctx)
    ctx sa "c"
  }

  // options
  @Test def testoptions = expect (true) {
    val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
    val scr = "java.lang.Sys"
    ctx.options (scr, scr.length()-1) contains ("System")
  }

  @Test def testdef = expect (5) {
    // simple, one time, expression
    val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
    ScalaScript ("""
def add (i:Int*) = {
  val ss = "12344"
  var ii = 0
  for (k <- i) ii = ii + k
  ii
  }
""").interactive (ctx)

    ScalaScript ("add (1+2, 8/4)").interactive (ctx) getOrElse "?"
  }

  @Test def testdefclass = expect (5) {
    // simple, one time, expression
    val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
    ScalaScript ("""
class A
class B

object O

def add (i:Int*) = {
  val ss = "12344"
  var ii = 0
  for (k <- i) ii = ii + k
  ii
  }
""").interactive (ctx)

    ScalaScript ("add (1+2, 8/4)").interactive (ctx) getOrElse "?"
  }

  @Test def testdefclass2 = expect (5) {
    // simple, one time, expression
    val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
    ScalaScript ("""class A""") interactive ctx
    ScalaScript ("""class B""") interactive ctx
    ScalaScript ("""object O""") interactive ctx
    ScalaScript ("""
def add (i:Int*) = {
  val ss = "12344"
  var ii = 0
  for (k <- i) ii = ii + k
  ii
  }
""").interactive (ctx)

    ScalaScript ("add (1+2, 8/4)").interactive (ctx) getOrElse "?"
  }

  @Test def testScripster = expect (3) {
    import razie.scripster._
    val c = Sessions.create (Scripster.sharedContext, "scala")
    Scripster.execWithin (10000) ("scala", "1+2", c.id)._2
  }

  @Test def testTimeout = expect (true) {
    import razie.scripster._
    val c = Sessions.create (Scripster.sharedContext, "scala")
    val res = razie.Timer {
      try {
        Scripster.execWithin (5000) ("scala", "while(true) print(\"\")", c.id)
      } catch { case _ => ; }
    }._1 
    res >= 5000 && res < 6000 
  }
  
  @Test def testHardTimeout = expect (true) {
    import razie.scripster._
    val c = Sessions.create (Scripster.sharedContext, "scala")
    val res = razie.Timer {
      try {
        Scripster.execWithin (5000) ("scala", "while(true) {} ", c.id)
      } catch { case _ => ; }
    }._1
    res >= 5000 && res < 6000 
  }
  
}

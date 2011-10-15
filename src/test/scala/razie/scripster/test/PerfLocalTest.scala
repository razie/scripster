package razie.scripster.test

import org.junit.Test
import razie.base.scripting._
import razie.scripster._
import org.scalatest.matchers.MustMatchers

class LocalPerfTest extends MustMatchers {

  @Test def testsimple100 {
    for (i <- 0 until 100) expect ("12") {
      val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
      ScalaScript ("val c = a+b").interactive (ctx)
      ScalaScript ("c").interactive (ctx) getOrElse "?"
    }
  }

  @Test def testdef100 {
    for (i <- 0 until 100) expect (5) {
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
  }

  @Test def testScripster100 {
    for (i <- 0 until 100) expect (3) {
      import razie.scripster._
      val c = Sessions.create (Scripster.sharedContext, "scala")
      Scripster.execWithin (10000) ("scala", "1+2", c.id)._2
    }
  }

}

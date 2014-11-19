package razie.scripster.test

import org.junit.Test
import razie.base.scripting._
import razie.scripster._
import org.scalatest.matchers.MustMatchers

class ScalaComplexTest extends MustMatchers {
  val SS = """
//select the following defs and issue "run selection"
class Num (val i:Int) {
           def + (other:Num) = new Num (this.i + other.i)
  override def toString      = "Numero " + i.toString
}

object Num { // this lets you do Num(3) instead of new Num(3)
  def apply (i:Int) = new Num (i)
}

implicit def toNum (i:Int) = Num (i) // this makes the conversion implicit
"""

  @Test def testdef1 = expect (3) {
    // simple, one time, expression
    val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
    ScalaScript (SS).interactive (ctx)

    ScalaScript ("1+2").interactive (ctx) getOrElse "?"
  }

  @Test def testTimeout = expect (RazScript.RSSucc(3)) {
    import razie.scripster._
    val c = Sessions.create (Scripster.sharedContext, "scala")
    Scripster.execWithin (50000) ("scala", SS, c.id)
    Scripster.execWithin (50000) ("scala", "1+2", c.id)._1
  }

}

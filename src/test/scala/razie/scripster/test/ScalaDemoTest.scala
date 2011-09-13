package razie.scripster.test

import org.junit.Test
import razie.base.scripting._
import razie.scripster._
import org.scalatest.matchers.MustMatchers

object Demo {
//crash-course into scala: select the following defs and issue "run selection"
class Num (val i:Int) {
           def + (other:Num)      = new Num (this.i + other.i) // operators
  override def toString : String  = "Numero " + i.toString     // inherit Java methods
//  override def equals (other:Any) = toString.equals(other.toString) // ugly hack :)
  override def equals (other:Any) = other match {
    case n:Num => n.i == i
    case _ => false
    }
}

object Num { // companion object contains statics for Num
  def apply (i:Int) = new Num (i)  // this lets you do Num(3) instead of new Num(3)
  def valueOf (s:String) = new Num (s.toInt)
}

implicit def toNum (i:Int) = Num (i) // this makes the conversion implicit

def add (i:Num*) = { // variable argument list
  val ss = "12344"   // type inferred as String
  var ii = Num(0)
  for (k <- i) ii = ii + k                  // boo hoo - the way of the java...
  assert (ii == i.foldLeft (Num(0)) (_+_))  // woo hoo - the way of the lambda ...
  ii
  }

val demo = """
//crash-course into scala: select the following defs and issue "run selection"
class Num (val i:Int) {
           def + (other:Num)      = new Num (this.i + other.i) // operators
  override def toString : String  = "Numero " + i.toString     // inherit Java methods
//  override def equals (other:Any) = toString.equals(other.toString) // ugly hack :)
  override def equals (other:Any) = other match {
    case n:Num => n.i == i
    case _ => false
    }
}

object Num { // companion object contains statics for Num
  def apply (i:Int) = new Num (i)  // this lets you do Num(3) instead of new Num(3)
  def valueOf (s:String) = new Num (s.toInt)
}

implicit def toNum (i:Int) = Num (i) // this makes the conversion implicit

def add (i:Num*) = { // variable argument list
  val ss = "12344"   // type inferred as String
  var ii = Num(0)
  for (k <- i) ii = ii + k                  // boo hoo - the way of the java...
  assert (ii == i.foldLeft (Num(0)) (_+_))  // woo hoo - the way of the lambda ...
  ii
  }
"""
}

class ScalaDemoTest extends MustMatchers {

  @Test def compiledShouldEqual3 = expect (Demo.Num(5)) {
    // test binding variables
    Demo.add (1+2, 8/4)
  }

  @Test def interpretedShouldEqual3 = expect (true) {
    // simple, one time, expression
    val ctx = ScalaScript.mkContext
    ScalaScript (Demo.demo).interactive (ctx) 
    ScalaScript ("val x = add (1+2, 8/4)").eval (ctx)
    ScalaScript ("x == Num(5)").eval (ctx) getOrElse false
  }

}

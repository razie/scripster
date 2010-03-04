package razie.scripster.test
import scala.tools.{nsc => nsc}

object CompletionTest {
  def main(args : Array[String]) : Unit = {
     
    val env = new nsc.Settings
    val p = new nsc.Interpreter (env)         
    val l = new java.util.ArrayList[String]()
    val c = new nsc.interpreter.Completion(p)
   
    val scr = "java.lang.Syste"
      
    c.jline.complete (scr, scr.length-1, l)
    c.jline.complete (scr, scr.length-1, l)
    println ("options for: \'"+scr+"\' are: " +l)

  }
}

/**  ____    __    ____  ____  ____/___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___) __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__)\__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)___/   (__)  (______)(____/   LICENESE.txt
 */
package razie.scripster.test

import scala.tools.{nsc => nsc}
import razie.base.scripting._

case class Bling (val s:String)

object CompletionTest {
  def main(args : Array[String]) : Unit = {
    val l = simple1
//    withContext
   import scala.collection.JavaConversions._
   
    val ret:List[Bling] = l.map (s=>Bling(s)).toList
    println ("MIMI IS: " +ret)
  }
  
  def withContext = {
    val s = new ScalaScriptContext()
    val scr = "java.lang.Syste"
       
    new Thread (new Runnable {
       override def run() : Unit = {
       val l = s.options(scr)
       println ("options for: \'"+scr+"\' are: " +l)
       }
    }).start()
  }
  
  def simple1 = {
    val env = new nsc.Settings
    val p = new nsc.Interpreter (env)         
    val l = new java.util.ArrayList[String]()
    val c = new nsc.interpreter.Completion(p)
   
    val scr = "java.lang.Syste"
      
    c.jline.complete (scr, scr.length-1, l)
    c.jline.complete (scr, scr.length-1, l)
//    println ("options for: \'"+scr+"\' are: " +l)
    l
  }
}

/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.scripster

import com.razie.pub.comms._
import com.razie.pub.base._
import com.razie.pub.base.data._
import com.razie.pub.base.ExecutionContext
import razie.base._
import razie.base.scripting._

/** run a simple scripseter server with no front-end */
object MainScripster {
   def main (argv:Array[String]) {
     Scripster.createServer(4445)
   
     // warm up the interpreter while you move your hands... :)
     new java.lang.Thread ( 
           new java.lang.Runnable { 
              def run() { 
                 new ScalaScript ("1+2").eval(ScriptContextImpl.global) 
                 }}
           ).start
     }
}

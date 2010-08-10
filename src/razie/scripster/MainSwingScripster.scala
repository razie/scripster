/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.scripster

//import com.razie.pub.lightsoa._
import com.razie.pub.comms._
import com.razie.pub.base._
import com.razie.pub.base.data._
//import com.razie.pub.http._
//import com.razie.pub.http.sample._
//import com.razie.pub.http.LightContentServer
import com.razie.pub.base.ExecutionContext
import razie.base._
import razie.base.scripting._

import razie.draw._
import razie.draw.samples._
import razie.draw.swing._
import scala.swing._
import razie.Draw

object MainSwingScripster extends SimpleSwingApplication {

  def top = new MainFrame {
    
   new java.lang.Thread ( 
         new java.lang.Runnable { 
            def run() { 
         // start the web server version in a separate thread to speed up 
               Scripster.createServer(4445)
               
     // warm up the scala compiler
               new ScriptScala ("1+2").eval(ScriptContextImpl.global) 
               }}
         ).start

         // draw a swing painting
         
    razie.draw.swing.Init.init
     
    title = "SCRIPSTER - Razie's ScriptPad"
       
    val panel = new BoxPanel (Orientation.Vertical) {
      def add (c:Component) { contents += c }
    }

   // can draw swing gadgets anywhere components can be added...
    val stream = new SwingDrawStream ({ c:Component => panel.add(c) })
    
    stream write ScripsterService.pad ("scala")
    
    contents = panel
    
  }
}  

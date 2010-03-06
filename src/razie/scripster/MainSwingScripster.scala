/**  ____    __    ____  ____  ____/___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___) __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__)\__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)___/   (__)  (______)(____/   LICENESE.txt
 */
package razie.scripster

import com.razie.pub.lightsoa._
import com.razie.pub.comms._
import com.razie.pub.base._
import com.razie.pub.base.data._
import com.razie.pub.http._
import com.razie.pub.http.sample._
import com.razie.pub.http.LightContentServer
import com.razie.pub.base.ExecutionContext
import razie.base._
import razie.scripting._

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
               new ScriptScala ("1+2").eval(ScriptContext.Impl.global) 
               }}
         ).start
   
   new java.lang.Thread ( 
         new java.lang.Runnable { 
            def run() { 
               Scripster.create(4445, None)
               }}
         ).start
   
     
    razie.draw.swing.Init.init
     
    title = "SCRIPSTER!"
       
    val panel = new BoxPanel (Orientation.Vertical) {
      def add (c:Component) { contents += c }
    }

    val stream = new SwingDrawStream ({ c:Component => panel.add(c) })
    
    stream write ScriptService.session ("scala")
    
    contents = panel
    
  }
}  

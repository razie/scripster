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

/** 
 * a door to the REPL
 * 
 * Usage: it works with a LightServer (see project razweb). You can {@link #attachTo} to an existing server or {@link create} a new, dedicated one.
 * 
 * If all you need in an app is the scripster, create one on a port of your choice, with no extra services.
 * 
 * To customize interaction, you can mess with the Repl - that's the actual interaction with the REPL
 * 
 * @author razvanc99
 */
object Scripster {

   // hook to the web/telnet server
   var contents : CS = null

   /** use this to attach the REPL to an existing server */
   def attachTo (ls:LightServer) = {
      contents = new CS(ls.contents.asInstanceOf[LightContentServer]) // TODO cleanup cast
      ls.contents = contents
   }
  
   /** create a new server on the specified port and start it on the thread */
   def create (port:Int, t:Option[(Runnable) => Thread], services:Seq[HttpSoaBinding] = Nil) {
      val ME = new AgentHandle("localhost", "localhost", "127.0.0.1", port
         .toString(), "http://localhost:" + port.toString());
      
      // stuff to set before you start the server
      HtmlRenderUtils.setTheme(new HtmlRenderUtils.DarkTheme());
      NoStatics.put(classOf[Agents], new Agents(new AgentCloud(ME), ME));

      val server = new LightServer (port, 20, ExecutionContext.instance(), new LightContentServer()) 
      val get = new MyServer()
      server.registerHandler(get)
      server.registerHandler(new LightCmdPOST(get))

      get.registerSoa(new HttpSoaBinding(ScriptService))
      services.foreach (get.registerSoa(_))
   
      attachTo(server)
  
      t match {
         case Some(mk) => mk (server).start()
         case None => server.run()
      }
   }
}

/** actual interaction with the REPL */
object Repl {
   ScriptFactory.init (new ScriptFactoryScala (null, true))
   
   def exec (lang:String, script:String, ctx:ScriptContext) : AnyRef = {
     razie.Log ("execute script=" + script)
     val s = ScriptFactory.make ("scala", script)
     s.eval(ctx)
   }
   
  def options (sessionId:String, line:String) = {
    val session = Sessions get sessionId
      
    val ret = 
      if (line endsWith "a") razie.AI("b") :: razie.AI("c") :: Nil
      else if (line endsWith "b") razie.AI("c") :: Nil
      else Nil
      
    razie.Debug ("options for: \'"+line+"\' are: " +ret)
    ret
  }
}

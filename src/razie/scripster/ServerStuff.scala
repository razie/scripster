/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
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
import razie.base.scripting._

/** a demo content server implementation */
class CS (proxy:LightContentServer) extends LightContentServer {
   if (proxy.dflt != null)
      throw new IllegalStateException ("server already has default handler, can't attach...")
   else  
      proxy.dflt = new SH()
   
   override def options (s:String, sessionId:String) : Seq[ActionItem] = {
      Scripster.options(sessionId, s)
   }
   
   override def exec(cmdLine:String, protocol:String, parms:java.util.Properties, socket:MyServerSocket, httpattrs:AttrAccess) : AnyRef = 
      proxy.exec (cmdLine, protocol, parms, socket, httpattrs)
      
   override def mkSession (): String = Sessions.create(Scripster.sharedContext).id 
}

/** default command handler - runs the entire line as a script */
class SH extends SocketCmdHandler {

   override def execServer(cmd:String , protocol:String , args:String , parms:java.util.Properties ,
                socket:MyServerSocket ) : AnyRef = {
     val script = if (args != null && args.length > 0) cmd + " " + args else cmd
     Scripster.exec ("scala", script, parms.getProperty("sessionId"))._2 
     }

   override def getSupportedActions() = Array[String]()
}

@SoaService (name="scripting", descr="scripging service", bindings=Array("http"))
object ScriptService {
   val cmdRSCRIPT = razie.AI ("run")
   val cmdRESET = razie.AI ("reset")
   
  @SoaMethod (descr="interactive", args=Array("sessionId", "line"))
  def options (sessionId:String, line:String) = 
    "[" + soptions(sessionId)(line).mkString (",") + "]"
  
  @SoaMethod (descr="exec a script", args=Array("sessionId", "language", "script"))
  def run (sessionId:String, language:String, script:String) = {
    Scripster.exec (language, script, sessionId)._2
  }
  
  @SoaMethod (descr="exec a script", args=Array("lang"))
  def session (lang:String) = {
     val c = Sessions create Scripster.sharedContext 
     new razie.draw.widgets.ScriptPad (mkATI(c.id) _, options=soptions(c.id) _, reset=mkRESET(c.id) _)
  }

  @SoaMethod (descr="exec a script", args=Array("lang"))
  def simpleSession (lang:String) = {
     val c = Sessions create Scripster.sharedContext 
     new razie.draw.widgets.ScriptPad (run=mkATI(c.id) _, options=soptions(c.id) _, reset=mkRESET(c.id) _, simple=true)
  }

  @SoaMethod (descr="exec a script", args=Array("lang"))
  def appletSession (lang:String) = {
     val c = Sessions create Scripster.sharedContext 
     new razie.draw.widgets.ScriptPad (run=mkATI(c.id) _, options=soptions(c.id) _, reset=mkRESET(c.id) _, applet=true)
  }

  @SoaMethod (descr="exec a script", args=Array("sessionId"))
  def reset (sessionId:String) = {
     Sessions.get(sessionId).map(_.clear)
  }

  def mkATI (sessionId:String)() : ActionToInvoke = {
    new ServiceActionToInvoke("scripting", cmdRSCRIPT, "sessionId", sessionId) {
       override def act (ctx:ActionContext) : AnyRef = {
          run (sa("sessionId"), sa("language"), sa("script"))
       }
    }
  }
  
  def mkRESET (sessionId:String)() : ActionToInvoke = {
    new ServiceActionToInvoke("scripting", cmdRESET, "sessionId", sessionId) {
       override def act (ctx:ActionContext) : AnyRef = {
          reset (sa("sessionId"))
       }
    }
  }
  
  private def soptions (sessionId:String)(s:String) : Seq[String] = {
    Scripster.options(sessionId, s).map (_.name)
  }

}

object Sessions {
  val life = 30 * 60 * 60 * 1000 // 30 minutes
  val map = razie.Mapi[String, ScriptSession] ()
  
  def create (parent:ScriptContext) = { 
    clean
    val c = new ScriptSession(parent)
    map.put(c.id, c)
    c 
  }
  
  def get (key:String) = {
    clean
    map.get(key)
  }
  
  def clean { 
    val time = System.currentTimeMillis
    val oldies = map.values.filter(_.time < time-life).toList
    oldies.foreach(x=>map.remove(x.id))
  }
}

// Sessions of scripting - maintain state
class ScriptSession (parent:ScriptContext) {
  val time = System.currentTimeMillis
  val id = time.toString // TODO use GUID
  val ctx = new ScalaScriptContext(parent)
  var buffer = new StringBuilder()
  var pcount = 0

  def script = {
    var s = buffer.toString
    // TODO idiot code but I'm bored of this right now, want to play piano instead
    if (s.indexOf("{") == 0 && s.lastIndexOf("}") == s.length-1) 
      s substring (1,s.length-1)
    else 
      s
  }

  // accumulate and identify statement blocks
  def accumulate (more:String) = {
    for (c <- more; if c=='{') pcount+=1
    for (c <- more; if c=='}') pcount-=1
    buffer append more
    if (pcount == 0) buffer append "\n" // must treat like a line
  }

  def inStatement = pcount > 0
  
  def clear { pcount = 0; buffer = new StringBuilder() }
}

class MyServer extends SimpleClasspathServer ("") {
   import scala.collection.JavaConversions._
        val pat = """/([^/]*).*""".r
   
  override def findSoaToCall(socket:MyServerSocket , path:String , parms:java.util.Properties ):String = {
     if (path != "/" && path != "") {
        val pat (svc) = path
        
        if (getBindings().filter(_.getServiceName().equals(svc)).isEmpty)
           null
        else path.replaceFirst("/", "")
     }
     else null
    }
}

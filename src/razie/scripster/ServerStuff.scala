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

/** a demo content server implementation */
class CS (proxy:LightContentServer) extends LightContentServer {
   if (proxy.dflt != null)
      throw new IllegalStateException ("server already has default handler, can't attach...")
   else  
      proxy.dflt = new SH()
   
   override def options (s:String, sessionId:String) : Seq[ActionItem] = {
      Repl.options(sessionId, s)
   }
   
   override def exec(cmdLine:String, protocol:String, parms:java.util.Properties, socket:MyServerSocket, httpattrs:AttrAccess) : AnyRef = 
      proxy.exec (cmdLine, protocol, parms, socket, httpattrs)
      
   override def mkSession (): String = Sessions.create.id 
}

class SH extends SocketCmdHandler {

   override def execServer(cmd:String , protocol:String , args:String , parms:java.util.Properties ,
                socket:MyServerSocket ) : AnyRef = {
     val script = if (args != null && args.length > 0) cmd + " " + args else cmd
     val sessionId = parms.getProperty("sessionId")
     Sessions.get (sessionId).map (session=>
           Repl.exec ("scala", script, session)).getOrElse("No session found id="+sessionId)
     }

   override def getSupportedActions() = Array[String]()
}

@SoaService (name="scripting", descr="scripging service", bindings=Array("http"))
object ScriptService {
   val cmdRSCRIPT = razie.AI ("run")
   
  @SoaMethod (descr="interactive", args=Array("sessionId", "line"))
  def options (sessionId:String, line:String) = 
    Repl.options(sessionId, line)
  
  @SoaMethod (descr="exec a script", args=Array("sessionId", "language", "script"))
  def run (sessionId:String, language:String, script:String) = {
     Sessions.get (sessionId).map (session=>
           Repl.exec (language, script, session)).getOrElse("No session found id="+sessionId)
  }
  
  @SoaMethod (descr="exec a script", args=Array("lang"))
  def session (lang:String) = {
     val c = Sessions.create
     new razie.draw.widgets.ScriptPad (mkATI(c.id) _)
  }

  @SoaMethod (descr="exec a script", args=Array("lang"))
  def simpleSession (lang:String) = {
     val c = Sessions.create
     new razie.draw.widgets.ScriptPad (run=mkATI(c.id) _, simple=true)
  }

  def mkATI (sessionId:String)() : ActionToInvoke = {
    new ServiceActionToInvoke("scripting", cmdRSCRIPT, "sessionId", sessionId) {
       override def act (ctx:ActionContext) : AnyRef = {
          run (sa("sessionId"), sa("language"), sa("script"))
       }
    }
  }

}

object Sessions {
  val life = 30 * 60 * 60 * 1000 // 30 minutes
  val map = razie.Mapi[String, ScriptSession] ()
  
  def create = { 
     clean
     val c = new ScriptSession()
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
class ScriptSession {
  val time = System.currentTimeMillis
  val id = time.toString // TODO use GUID
  val ctx = new ScalaScriptContext(ScriptContext.Impl.global)
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

  def add (more:String) : Boolean = {
    for (c <- more; if c=='{') pcount+=1
    for (c <- more; if c=='}') pcount-=1
    buffer append more
    if (pcount == 0) buffer append "\n" // must treat like a line
    pcount == 0 // true if can execute
  }
  
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

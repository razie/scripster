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
import razie.Draw
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
      
   override def mkSession (lang:String): String = Sessions.create(Scripster.sharedContext, lang).id 
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

/**  
 * allow services to be called without a prefix. http://localhost:1234/service/method... 
 */
class SimpleGetHandler extends SimpleClasspathServer ("") {
   import scala.collection.JavaConversions._
        val pat = """([^/]*).*""".r
   
  override def findSoaToCall(socket:MyServerSocket , path:String , parms:java.util.Properties ):String = {
     if (path != "") {
        val pat (svc) = path
        
        if (getBindings().filter(_.getServiceName().equals(svc)).isEmpty)
           null
        else path
     }
     else null
    }
}

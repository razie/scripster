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

/** 
 * a door to the REPL
 * 
 * Usage: it works with a LightServer (see project razweb). You can 
 * {@link #attachTo} to an existing server or {@link create} a new, dedicated one.
 * 
 * If all you need in an app is the scripster, create one on a port of your choice, with no extra services.
 * 
 * To customize interaction, you can mess with the Repl - that's the actual interaction with the REPL
 *
 * Further customization options below.
 * 
 * To limit the objects available to clients, please reset the sharedContext:
 * <code>
 * val myContext = ScriptFactory.mkContext(null) // null means no parent for this one
 * myContext.put ("obj1", obj1)
 * ... // add more objects 
 * Scripster.sharedContext = myContext
 * </code>
 * 
 * NOTE that this is truly a shared context, shared across sessions. Changing the state of objects 
 * here will be shared.
 * 
 * However, each individual session gets its own context, so new objects created in one session are not
 * visible to others.
 * 
 * @author razvanc99
 */
object Scripster {
   // set this to limit the objects to use...
   var sharedContext = ScriptContextImpl.global
   
   // hook to the web/telnet server
   private var contents : CS = null
   
   ScriptFactory.init (new ScalaScriptFactory(ScriptFactory.singleton, true))

   /** use this to attach the REPL to an existing server */
   def attachTo (ls:LightServer) = {
      contents = new CS(ls.contents.asInstanceOf[LightContentServer]) // TODO cleanup cast
      ls.contents = contents
   }
  
   /** create a new server on the specified port and start it on the thread 
    * 
    * @param port the port number to start the embedded server at
    * @param runner - if provided will be used to ru nthe server, otherwise new thread is spawned
    * @param services - extra services to be added to the embedded server
    */
   def createServer (port:Int, runner:Option[(Runnable) => Thread]=None, services:Seq[HttpSoaBinding] = Nil) {
      val ME = new AgentHandle("localhost", "localhost", "127.0.0.1", port
         .toString(), "http://localhost:" + port.toString());
      
      // stuff to set before you start the server
      HtmlRenderUtils.setTheme(new HtmlRenderUtils.DarkTheme());
      NoStatics.put(classOf[Agents], new Agents(new AgentCloud(ME), ME));
     
      // default auth is configuration-based. 
      // let's allow everyone, instead
      LightAuth.underLockAndKey {
        LightAuth.init()
        LightAuth.ipMatches (".*", LightAuthType.INCLOUD)
      }

      // start the web server with the default content server
      val server = new LightServer (port, 20, ExecutionContext.instance(), new LightContentServer()) 
      
      // add some handlers
      val get = new SimpleGetHandler()
      server.registerHandler(get)
      server.registerHandler(new LightCmdPOST(get))

      if (System.getProperty("scripsterpro.run", "true").equals("true")) {
        get.registerSoa(new HttpSoaBinding(ScripsterService))
        get.registerSoa(new HttpSoaBinding(ScriptService))
      }
      
      services.foreach (get.registerSoa(_))
   
      attachTo(server)
  
      runner match {
         case Some(mk) => mk (server).start()
         case None => server.run()
      }
   }

  /** create new session - not neccessary to just run scripts */
  def createSession (lang:String, ctx:ScriptContext = sharedContext) = 
    Sessions.create(ctx, lang).id

  /** reset an existing session */   
  def reset (sessionId:String) = Sessions.get(sessionId).map(_.clear)

  /** session-based execution 
   * 
   * @return (complex code with info , just null or value) 
  */
  def exec (lang:String, script:String, sessionId:String) : (RazScript.RSResult[Any], AnyRef) = {
//     var ret : (RazScript.RSResult[Any], AnyRef) = (RazScript.RSUnsupported ("Scripster.exec() TODO"), null)
//     ret
     texec (lang, script, sessionId)
  }

   def execWithin (msec:Int) (lang:String, script:String, sessionId:String) : (RazScript.RSResult[Any], AnyRef) = {
     val x = 
     (razie.Threads.forkjoinWithin[String,(RazScript.RSResult[Any], AnyRef)] (msec) (List(script)) { 
        Scripster.exec (lang, _, sessionId)
        })
println ("X ========= " + x)
     if (x != null && x.size > 0 && x.head._1 != null) x.head
     else ( RazScript.RSError("UNKNOWN - Probably did not finish in time..."), null)
   }

   /** this runs in the current thread
    * 
    * @return (complex code with info , just null or value) 
    */
   def texec (lang:String, script:String, sessionId:String) : (RazScript.RSResult[Any], AnyRef) = {
      val ret = try {
     Sessions.get (sessionId).map (session=> {
     session accumulate script
     
     val s = ScriptFactory.make (lang, session.script)
     razie.Log ("execute script=" + session.script)
     Audit.recRun(lang, "", "", "", session.script)
     
     s.interactive(session.ctx) match {
       case s1@RazScript.RSSucc(res) => {
          session.clear
          (s1, res.asInstanceOf[AnyRef])
       }
       case s2@RazScript.RSSuccNoValue => {
          session.clear
          (s2, null)
       }
       case s3@RazScript.RSError(err) => {
          val errmsg = err
          razie.Debug ("SError...: "+errmsg)
          session.clear
          (s3, errmsg)
       }
       case s4@RazScript.RSIncomplete => {
          razie.Debug ("SIncomplete...accumulating: "+script)
          (s4, null)
       }
       case s5@RazScript.RSUnsupported (msg) => {
          // do the accumulation ourselves
         if (! session.inStatement) {
           val s = ScriptFactory.make ("scala", session.script)
           razie.Log ("execute script=" + session.script)
           session.clear
           val ret = s.eval(session.ctx)
           (ret, ret)
          }
         else 
            (s5, null)
       }
     }
     }
     ).getOrElse((RazScript.RSError("No session found id="+sessionId), "No session found id="+sessionId))
      } catch {
          case e:Exception => {
         razie.Log ("While processing script: " + script, e)
            val r = "ERROR: " + e.getMessage 
            (RazScript.RSError(r), null)
          }
      }
          
     Audit.recResult(lang, "", "", "", script, ""+ret)
     
     razie.Log ("result=" + ret)
     ret
   }
   
  def options (sessionId:String, line:String) = {
    Sessions get sessionId match {
       case Some(session) => {
    val l = session.ctx.options (line)

    import scala.collection.JavaConversions._
   
    val ret:List[razie.AI] = l.map (s=>razie.AI(s)).toList
   
    Audit.recOptions(session.lang, line)
    razie.Debug ("options for: \'"+line+"\' are: " +ret)
    ret
       }
       case None => Nil
    }
  }
}

object Audit {
  private def from : String = 
     Option(ExecutionContext.instance().a("httpattrs")).map(_.asInstanceOf[AttrAccess] sa "RemoteIP").getOrElse("x.x.x.x") +
     "/" +
     Option(ExecutionContext.instance().a("httpattrs")).map(_.asInstanceOf[AttrAccess] sa "X-Forwarded-For").getOrElse("x.x.x.x")

  private def audit (aa:AttrAccess) = razie.Audit (aa)

  def enc (s:String) = Comms.encode(s)
  
  def recSessionFail  (lang:String, info:String="") {// new session
     audit (razie.AA("event","sessionFail", "lang", lang, "from",from, "info", info))
  }
  def recSession  (lang:String) {// new session
     audit (razie.AA("event","session", "lang", lang, "from",from))
  }
  def recOptions  (lang:String, line:String) {
     audit (razie.AA("event","options", "lang",lang,"from",from, "line",enc(line)))
  }
  def recRun (lang:String, ok:String, k:String, api_key:String, script:String) {
     audit (razie.AA("event","fork", "lang",lang,"ok", ok, "k", k, "api_key",api_key,"from",from,"script",enc(script)))
  }
  def recResult (lang:String, ok:String, k:String, api_key:String, script:String, result:String) {
     audit (razie.AA("event","fork", "lang",lang,"ok", ok, "k", k, "api_key",api_key,"from",from,"script",enc(script), "result", enc(result)))
  }
}


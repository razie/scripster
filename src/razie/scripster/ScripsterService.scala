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

/** @deprecated */
@SoaService (name="scripting", descr="scripging service", bindings=Array("http"))
object ScriptService {
   
  // intercept all possible calls to this service and redirect
  @SoaMethod (descr="interactive") // need this
  @SoaMethodSink // sink all calls
  @SoaAllParms // accept any parm
  def deprecated (parms:AttrAccess) = 
     razie.Draw html "DEPRECATED. please use /scripster/... instead of /scripting/... <a href=\"/scripster/session\">like so</a>"
}

/** the main scripster service (aka serlvet) */
@SoaService (name="scripster", descr="scripging service", bindings=Array("http"))
object ScripsterService {
   val cmdRSCRIPT = razie.AI ("run")
   val cmdRESET = razie.AI ("reset")
   
  @SoaMethod (descr="interactive", args=Array("sessionId", "line"))
  def options (sessionId:String, line:String) = 
    razie.Draw.html("[" + soptions(sessionId)(line).map ("\""+_+"\"").mkString (",") + "]")
  
  @SoaMethod (descr="exec a script", args=Array("sessionId", "language", "script"))
  def run (sessionId:String, language:String, script:String) = {
    val ret = Scripster.exec (language, script, sessionId)
    ret._1 match {
       case s1@RazScript.RSSucc(res) => Draw toString res.toString
       case s2@RazScript.RSSuccNoValue => Draw toString "Scripster.Status:...ok"
       case s3@RazScript.RSError(err) => Draw toString "Error: " + err
       case s4@RazScript.RSIncomplete => Draw toString "Scripster.Status:...incomplete"
       case s4@RazScript.RSUnsupported(msg) => Draw toString "Scripster.Status:...Unsupported: " + msg
       case _ => Draw text "Scripster.Status:??? the interpreter said what ??? : " + ret._1 .toString
     }
  }
  
  @SoaMethod (descr="create a new session and a simple pad", args=Array("lang"))
  def pad (lang:String) = {
    val c = Sessions.create (Scripster.sharedContext , lang)
    new razie.draw.widgets.ScriptPad (lang=lang, run=mkATI(c.id) _, options=soptions(c.id) _, reset=mkRESET(c.id) _)
  }

  def j(lang:String, ok:String, k:String, ak:String) = "','"+lang+"', '"+ok+"', '"+k+"', '"+ak+"')"

  @SoaMethod (descr="exec a script", args=Array("lang"))
  @SoaMethodSink
  def session (ilang:String) = {
    val notice = Comms.readStream (this.getClass().getResource("/public/scripster.html").openStream)
    val title = razie.Draw html "<b> Scripster - interactive scala script pad</b>"

    val lang = Option(ilang) getOrElse "scala"
    
    val p = pad(lang)
    p.moreButtons = 
      Draw.button(razie.AI("Witty"),   "javascript:scripsterJump('http://codewitter.com/cw/start?from=scripster"+j(lang,"", "", "")) :: Nil 
//      Draw.button(razie.AI("Witty"),   "javascript:scripsterJump('/cw/start?from=scripster"+j(lang,"", "", "")) :: Nil 

    if (notice != null && notice.length > 0 ) 
       razie.Draw seq ( title, p, razie.Draw html "<p><b>Notice</b>", razie.Draw htmlMemo notice)
    else 
       razie.Draw seq (title, p)
  }

  @SoaMethod (descr="exec a script", args=Array("lang"))
  def simpleSession (lang:String) = {
     val c = Sessions.create(Scripster.sharedContext , lang)
     new razie.draw.widgets.ScriptPad (lang=lang, run=mkATI(c.id) _, options=soptions(c.id) _, reset=mkRESET(c.id) _, simple=true)
  }

  @SoaMethod (descr="exec a script", args=Array("lang"))
  def appletSession (lang:String) = {
     val c = Sessions.create(Scripster.sharedContext , lang)
     new razie.draw.widgets.ScriptPad (lang=lang, run=mkATI(c.id) _, options=soptions(c.id) _, reset=mkRESET(c.id) _, applet=true)
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

/** simple session manager - sessions expire in 7 minutes */
object Sessions {
  val life = intprop("scripster.sessions.life", 3 * 60) * 1000L // 2 minutes
  val max = intprop("scripster.sessions.max", 12) // 2 minutes
  val map = razie.Mapi[String, ScriptSession] ()

  def intprop (prop:String, dflt:Int) : Int = {
     val s = System.getProperty(prop)
     
     try {
        Integer.parseInt(s)
     } catch {
        case e:Throwable => dflt
     }
  }

  def create (parent:ScriptContext, lang:String) = { 
     
    clean

    if (map.size >= max) {
    val now = System.currentTimeMillis
       Audit.recSessionFail (lang)
       error ("too many sessions - come back...later, sessions=" + map.size + 
              "  - patience, one expires in: " + (life-(now-(map.values.map(_.time).foldRight(now)((x,y)=>if (x<y)x else y))))/(1000L) + "sec" )
    }
       
    Audit.recSession (lang)
    val c = new ScriptSession(parent, lang)
    map.put(c.id, c)
    c 
  }
  
  def get (key:String) = {
    val c = map.get(key)
    c.foreach(_.time = System.currentTimeMillis) // reset last access
    c
  }
  
  def clean { 
    val time = System.currentTimeMillis
    val oldies = map.values.filter(_.time < time-life).toList
    oldies.foreach(x=>map.remove(x.id))
  }
}

/** Sessions of scripting - maintain state */
class ScriptSession (parent:ScriptContext, val lang:String) {
  var time = System.currentTimeMillis
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

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
import razie.draw.widgets.NavLink
import razie.base.scripting._

/** the main scripster service (aka serlvet) */
@SoaService(name = "scripster", descr = "scripging service", bindings = Array("http"))
object ScripsterService extends ScripsterService 

class ScripsterService {
  val cmdRSCRIPT = razie.AI ("run")
  val cmdRESET = razie.AI ("reset")

  /** serve the current options as json */
  @SoaMethod(descr = "interactive", args = Array("sessionId", "line"))
  def options(sessionId: String, line: String) =
    razie.Draw.html("[" + soptions(sessionId)(line).map(_.replaceAll("\\\\", "\\\\\\\\")).map ("\"" + _ + "\"").mkString (",") + "]")

  @SoaMethod(descr = "exec a script", args = Array("sessionId", "language", "script"))
  def run(sessionId: String, language: String, script: String) = {
    val ret = Scripster.execWithin (90000) (language, script, sessionId)

    ret._1 match {
      case s1@RazScript.RSSucc(res) => Option(res) map (x => Draw toString x.toString) getOrElse (Draw text "<ERROR: could not retrieve result - is null!>")
      case s2@RazScript.RSSuccNoValue => Draw toString "Scripster.Status:...ok"
      case s3@RazScript.RSError(err) => Draw toString "Error: " + err
      case s4@RazScript.RSIncomplete => Draw toString "Scripster.Status:...incomplete"
      case s4@RazScript.RSUnsupported(msg) => Draw toString "Scripster.Status:...Unsupported: " + msg
      case _ => Draw text "Scripster.Status:??? the interpreter said what ??? : " + ret._1.toString
    }
  }

//  @SoaMethod (descr="create a new session and a simple pad", args=Array("lang", "initial"))
  def pad (lang:String, initial:String = null) = {
    val c = Sessions.create(Scripster.sharedContext, lang)
    if (initial == null || initial == "")
      new razie.draw.widgets.ScriptPad (lang=lang,run=mkATI(c.id) _, options=soptions(c.id) _, reset=mkRESET(c.id) _)
    else
      new razie.draw.widgets.ScriptPad (lang=lang,run=mkATI(c.id) _, options=soptions(c.id) _, reset=mkRESET(c.id) _, initial=initial)
  }


  def j(lang: String, ok: String, k: String, ak: String) = "','" + lang + "', '" + ok + "', '" + k + "', '" + ak + "')"

  @SoaMethod (descr="start a scripting session", args=Array("lang", "initial"))
  @SoaMethodSink
  def session (ilang:String, initial:String) = {
    val notice = Comms.readStream (this.getClass().getResource("/public/scripster.html").openStream)
    class AI(name: String, label: String, tooltip: String, iconP: String = razie.Icons.UNKNOWN.name)
    val titleb = razie.Draw button (new razie.AI(name = "xx", label = "", iconP = "/public/small_logog.PNG", tooltip = ""), "http://scripster.razie.com")
    titleb.style(NavLink.Style.JUST_ICON, NavLink.Size.SMALL)
    val title = razie.Draw.table (2) (titleb, razie.Draw html "<b>Scripster - interactive scala script pad</b>") align (razie.draw.Align.LEFT)
    title.packed = true
    //    title.valign = "center"
    //    val title = razie.Draw html "<img height=30 width=30 src=\"/public/small_logog.PNG\"/><b> Scripster - interactive scala script pad</b>"

    val lang = Option(ilang) getOrElse "scala"

    val p = pad(lang, initial)
    p.moreButtons =
      Draw.button(razie.AI("Witty"), "javascript:scripsterJump('http://cw.razie.com/cw/start?from=scripster" + j(lang, "", "", "")) :: Nil
    //      Draw.button(razie.AI("Witty"),   "javascript:scripsterJump('/cw/start?from=scripster"+j(lang,"", "", "")) :: Nil 

    if (notice != null && notice.length > 0)
      razie.Draw seq (title, p, razie.Draw html "<p><b>Notice</b>", razie.Draw htmlMemo notice)
    else
      razie.Draw seq (title, p)
  }

  @SoaMethod(descr = "create a simple session", args = Array("lang"))
  def simpleSession(lang: String) = {
    val id = Scripster.createSession(lang, Scripster.sharedContext)
    new razie.draw.widgets.ScriptPad(lang = lang, run = mkATI(id) _, options = soptions(id) _, reset = mkRESET(id) _, simple = true)
  }

  @SoaMethod(descr = "create an applet session", args = Array("lang"))
  def appletSession(lang: String) = {
    val id = Scripster.createSession(lang, Scripster.sharedContext)
    new razie.draw.widgets.ScriptPad(lang = lang, run = mkATI(id) _, options = soptions(id) _, reset = mkRESET(id) _, applet = true)
  }

  @SoaMethod(descr = "reset the session", args = Array("sessionId"))
  def reset(sessionId: String) = Scripster reset sessionId

  def mkATI(sessionId: String)(): ActionToInvoke = {
    new ServiceActionToInvoke("scripting", cmdRSCRIPT, "sessionId", sessionId) {
      override def act(ctx: ActionContext): AnyRef = {
        run (sa("sessionId"), sa("language"), sa("script"))
      }
    }
  }

  def mkRESET(sessionId: String)(): ActionToInvoke = {
    new ServiceActionToInvoke("scripting", cmdRESET, "sessionId", sessionId) {
      override def act(ctx: ActionContext): AnyRef = {
        reset (sa("sessionId"))
      }
    }
  }

  private def soptions(sessionId: String)(s: String): Seq[String] = {
    Scripster.options(sessionId, s).map (_.name)
  }

}

/** @deprecated */
@SoaService(name = "scripting", descr = "scripging service", bindings = Array("http"))
object ScriptService {

  // intercept all possible calls to this service and redirect
  @SoaMethod(descr = "interactive") // need this
  @SoaMethodSink // sink all calls
  @SoaAllParms // accept any parm
  def deprecated(parms: AttrAccess) =
    razie.Draw html "DEPRECATED. please use /scripster/... instead of /scripting/... <a href=\"/scripster/session\">like so</a>"
}


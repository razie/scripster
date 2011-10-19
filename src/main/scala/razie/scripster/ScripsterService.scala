/** ____    __    ____  ____  ____,,___     ____  __  __  ____
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
import razie.draw.DrawStream
import razie.draw.HttpDrawStream
import razie.draw.widgets.ScriptPad
import razie.draw.widgets.ScriptPad2

/** the main scripster service (aka serlvet) */
@SoaService(name = "scripster", descr = "scripging service", bindings = Array("http"))
object ScripsterService extends ScripsterService {
  def resource(name: String): String =
    Option(this.getClass().getResource(name)).
      map(_.openStream).
      map(Comms.readStream(_)).getOrElse("ERR Notice resource missing")

}

class ScripsterService {
  val cmdRSCRIPT = razie.AI("run")
  val cmdRESET = razie.AI("reset")

  /** serve the current options as json */
  @SoaMethod(descr = "interactive", args = Array("sessionId", "pos", "line"))
  def options(sessionId: String, pos: String, line: String) = {
    val position = if (pos.length <=0) line.length()-1 else pos.toInt
    Draw.html("[" + soptions(sessionId)(line, position).map(_.replaceAll("\\\\", "\\\\\\\\")).map("\"" + _ + "\"").mkString(",") + "]")
  }

  @SoaMethod(descr = "exec a script", args = Array("sessionId", "language", "script"))
  def run(sessionId: String, language: String, script: String) = {
    val ret = Scripster.execWithin(90000)(language, script, sessionId)

    ret._1 match {
      case s1 @ RazScript.RSSucc(res)        => Option(res) map (Draw toString _.toString) getOrElse (Draw text "<ERROR: could not retrieve result - is null!>")
      case s2 @ RazScript.RSSuccNoValue      => Draw toString "Scripster.Status:...ok"
      case s3 @ RazScript.RSError(err)       => Draw toString "Error: " + err
      case s4 @ RazScript.RSIncomplete       => Draw toString "Scripster.Status:...incomplete"
      case s4 @ RazScript.RSUnsupported(msg) => Draw toString "Scripster.Status:...Unsupported: " + msg
      case _                                 => Draw text "Scripster.Status:??? the interpreter said what ??? : " + ret._1.toString
    }
  }

  def mkPad(
    lang: String,
    css: String = "dark", // it's dark/light for now
    applet: Boolean = false,
    initial: String = ScriptPad.INITIAL,
    sessionId: String = "0",
    moreButtons: List[NavLink] = Nil,
    makeButtons: () => Seq[NavLink] = () => Nil) = {
    val scr = if (initial == null || initial.length() == 0) ScriptPad.INITIAL else initial
    if (msie.getOrElse(false)) {
      val p = new ScriptPad(lang = lang, run = mkATI(sessionId) _, options = soptions(sessionId) _, reset = mkRESET(sessionId) _, initial = scr,
        css = css, moreButtons = moreButtons, makeButtons = makeButtons)
      Draw.seq(Draw html "NOTE: behaves and looks better on Chrome or Firefox :)", p)
    } else {
      val p = new ScriptPad2(lang = lang, run = mkATI(sessionId) _, options = soptions(sessionId) _, reset = mkRESET(sessionId) _, initial = scr,
        css = css, moreButtons = moreButtons, makeButtons = makeButtons)
      Draw.seq(p)
    }
  }

  def pad(lang: String, initial: String = ScriptPad.INITIAL) = {
    val c = Sessions.create(Scripster.sharedContext, lang)
    new ScriptPad(lang = lang, run = mkATI(c.id) _, options = soptions(c.id) _, reset = mkRESET(c.id) _, initial = initial)
  }

  def pad2(lang: String, initial: String = ScriptPad.INITIAL) = {
    val c = Sessions.create(Scripster.sharedContext, lang)
    new ScriptPad2(lang = lang, run = mkATI(c.id) _, options = soptions(c.id) _, reset = mkRESET(c.id) _, initial = initial)
  }

  def j(lang: String, ok: String, k: String, ak: String) = "','" + lang + "', '" + ok + "', '" + k + "', '" + ak + "')"

  def mkTitle = {
//    class AI(name: String, label: String, tooltip: String, iconP: String = razie.Icons.UNKNOWN.name)
    val titleb = razie.Draw button (new razie.AI(name = "xx", label = "", iconP = "/public/small_logog.PNG", tooltip = ""), "http://scripster.razie.com")
    titleb.style(NavLink.Style.JUST_ICON, NavLink.Size.SMALL)
    val title = razie.Draw.table(2)(titleb, razie.Draw html "<b>Scripster - interactive scala script pad</b>") align (razie.draw.Align.LEFT)
    title.packed = true
    title
  }

  def msie = for (
    h <- ExecutionContext.instance().get[AttrAccess]("httpattrs");
    ua <- h.get[String]("User-Agent")
  ) yield ua contains "MSIE"

  @SoaMethod(descr = "start a scripting session", args = Array("lang"))
  @SoaStreamable(mime = "text") // otherwise it gets wrapped in an html <body> with CSS and everything
  def apisessioncreate(out: DrawStream, ilang: String) = {
    val lang = Option(ilang) getOrElse "scala"
    val id = Scripster.createSession(lang, Scripster.sharedContext)
    out write id
  }

  @SoaMethod(descr = "close a scripting session", args = Array("sessionId"))
  def apisessionclose(sessionId: String) = {
    Scripster.closeSession(sessionId)
  }

  @SoaMethod(descr = "exec a script", args = Array("sessionId", "language", "script"))
  @SoaStreamable(mime = "text") // otherwise it gets wrapped in an html <body> with CSS and everything
  def apirun(out: DrawStream, sessionId: String, language: String, script: String) = {
    val ret = run(sessionId, language, script)
    out write ret
  }

  @SoaMethod(descr = "start a scripting session", args = Array("lang", "initial"))
  @SoaStreamable
  @SoaMethodSink
  def session(out: DrawStream, ilang: String, initial: String) = {
    val notice = ScripsterService.resource ("/public/scripster.html")

    val title = mkTitle

    val lang = Option(ilang) getOrElse "scala"

    val moreButtons =
      Draw.button(razie.AI("Witty"), "javascript:scripsterJump('http://cw.razie.com/cw/start?from=scripster" + j(lang, "", "", "")) :: Nil

    val c = Sessions.create(Scripster.sharedContext, lang)
    val temp = mkPad (lang = lang, initial = initial, sessionId = c.id, moreButtons = moreButtons)

    val res = if (notice != null && notice.length > 0)
      razie.Draw seq (title, temp, razie.Draw html "<p><b>Notice</b>", razie.Draw htmlMemo notice)
    else
      razie.Draw seq (title, temp)

    // google ranking is relevant?
    out.asInstanceOf[HttpDrawStream].addMeta ("<title>Try and test scala code - interactive scala script pad</title>")
    out write res
  }

  @SoaMethod(descr = "start a scripting session with OLD scriptpad", args = Array("lang", "initial"))
  @SoaMethodSink
  def session1(ilang: String, initial: String) = {
    val notice = ScripsterService.resource ("/public/scripster.html")
    class AI(name: String, label: String, tooltip: String, iconP: String = razie.Icons.UNKNOWN.name)
    val titleb = razie.Draw button (new razie.AI(name = "xx", label = "", iconP = "/public/small_logog.PNG", tooltip = ""), "http://scripster.razie.com")
    titleb.style(NavLink.Style.JUST_ICON, NavLink.Size.SMALL)
    val title = razie.Draw.table(2)(titleb, razie.Draw html "<b>Scripster - interactive scala script pad</b>") align (razie.draw.Align.LEFT)
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
    new razie.draw.widgets.ScriptPad2(lang = lang, run = mkATI(id) _, options = soptions(id) _, reset = mkRESET(id) _, applet = true)
  }

  @SoaMethod(descr = "reset the session", args = Array("sessionId"))
  def reset(sessionId: String) = Scripster reset sessionId

  def mkATI(sessionId: String)(): ActionToInvoke = {
    new ServiceActionToInvoke("scripting", cmdRSCRIPT, "sessionId", sessionId) {
      override def act(ctx: ActionContext): AnyRef = {
        run(sa("sessionId"), sa("language"), sa("script"))
      }
    }
  }

  def mkRESET(sessionId: String)(): ActionToInvoke = {
    new ServiceActionToInvoke("scripting", cmdRESET, "sessionId", sessionId) {
      override def act(ctx: ActionContext): AnyRef = {
        reset(sa("sessionId"))
      }
    }
  }

  private def soptions(sessionId: String)(s: String, pos: Int): Seq[String] = {
    Scripster.options(sessionId, s, pos).map(_.name)
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


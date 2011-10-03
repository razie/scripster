/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.scripsterpro

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
import razie.scripster.ScripsterService._
import razie.Draw
import org.json.JSONObject
import razie.XP
import razie.AI
import razie.scripster._
import razie.draw.widgets.NavLink

/** API CANNOT change - they will be exposed to public. see cw_api.html for exact API defn */
@SoaService(name = "cw", descr = "Pro scripting service", bindings = Array("http"))
object CodeWitterService {
  object Audit {
    private def from: String =
      Option(ExecutionContext.instance().a("httpattrs")).map(_.asInstanceOf[AttrAccess] sa "RemoteIP").getOrElse("x.x.x.x") +
        "/" +
        Option(ExecutionContext.instance().a("httpattrs")).map(_.asInstanceOf[AttrAccess] sa "X-Forwarded-For").getOrElse("x.x.x.x")

    private def audit(aa: AttrAccess) = razie.Audit(aa)

    def enc(s: String) = Comms.encode(s)

    def recNew(lang: String, k: String, api_key: String, css: String, script: String) {
      audit(razie.AA("event", "new", "lang", lang, "k", k, "api_key", api_key, "css", css, "from", from, "script", enc(script)))
    }
    def recCapture1(lang: String, k: String, api_key: String, css: String, script: String) {
      audit(razie.AA("event", "capture1", "lang", lang, "k", k, "api_key", api_key, "css", css, "from", from, "script", enc(script)))
    }
    def recView(lang: String, k: String, css: String, script: String) {
      audit(razie.AA("event", "view", "lang", lang, "k", k, "css", css, "from", from, "script", enc(script)))
    }
    def recEmbed(lang: String, ok: String, css: String, script: String) {
      audit(razie.AA("event", "embed", "lang", lang, "ok", ok, "css", css, "from", from, "script", enc(script)))
    }
    def recFork(lang: String, ok: String, k: String, api_key: String, css: String, script: String) {
      audit(razie.AA("event", "fork", "lang", lang, "ok", ok, "k", k, "api_key", api_key, "css", css, "from", from, "script", enc(script)))
    }
    def recFork1(lang: String, ok: String, k: String, api_key: String, css: String, script: String) {
      audit(razie.AA("event", "fork1", "lang", lang, "ok", ok, "k", k, "api_key", api_key, "css", css, "from", from, "script", enc(script)))
    }
    def recStart1() {
      audit(razie.AA("event", "start1", "from", from))
    }
    def recStart2(lang: String, orig_api_key: String, api_key: String, css: String, script: String) {
      audit(razie.AA("event", "start2", "lang", lang, "orig_api_key", orig_api_key, "api_key", api_key, "css", css, "from", from, "script", enc(script)))
    }

    def recHash(email: String, pub_api_key: String, pri_api_key: String) {
      audit(razie.AA("event", "hash", "email", email, "pub_api_key", pub_api_key, "pri_api_key", pri_api_key, "from", from))
    }
  }

  val cmdRSCRIPT = razie.AI("run")
  val cmdRESET = razie.AI("reset")

  /** simplest view - just the pad with links - it's a simple syntax-colored pad, view only  */
  @SoaMethod(descr = "view a script", args = Array("lang", "ok", "css", "script"))
  def embed(lang: String, ok: String, css: String, script: String) = {
    val s = if (script == null) "" else script

    Audit.recEmbed(lang, ok, css, script)

    val lines = script.split('\n')
    val r = lines.size
    val c = script.filter(_ == '\n').size + 1

    val p = new razie.draw.widgets.SimpleScriptPad(
      lang = lang,
      css = css,
      makeButtons = mkEmbed(lang, ok, css, script) _,
      content = script,
      readOnly = true)
    // TODO i don't know why it doens't work with this on the line above
    p.rows(r)

    p
  }

  /** view a script - it's a simple syntax-colored pad, with some text, view only */
  @SoaMethod(descr = "view a script", args = Array("lang", "k", "css", "script"))
  def view(lang: String, k: String, css: String, script: String) = {

    Audit.recView(lang, k, css, script)

    val p = new razie.draw.widgets.SimpleScriptPad(
      lang = lang,
      css = css,
      makeButtons = mkFork(lang, k, "", ApiKey.genForMe, css, script) _,
      content = script)

    razie.Draw seq (
      razie.Draw html "<p><b>Code Witter - tweet your witty code</b>",
      p,
      razie.Draw htmlMemo Pro.notice,
      razie.Draw htmlMemo Pro.blahblah)
  }

  /** view a script - it's a simple syntax-colored pad, with some text, view only */
  @SoaMethod(descr = "view a script", args = Array("lang", "ok", "k", "api_key", "css", "script"))
  def create(lang: String, ok: String, k: String, api_key: String, css: String, script: String) = {
    Audit.recCapture1(lang, k, api_key, css, script)

    val p = new razie.draw.widgets.SimpleScriptPad(
      lang = lang,
      css = css,
      makeButtons = mkCreate(lang, (if (ok == null) "" else ok), k, api_key, script, css) _,
      content = script)

    razie.Draw seq (
      razie.Draw html "<p><b>Code Witter - tweet your witty code</b>",
      p,
      razie.Draw htmlMemo Pro.notice,
      razie.Draw htmlMemo Pro.blahblah)
  }

  def mkEmbed(lang: String, ok: String, css: String, script: String)(): Seq[NavLink] = {
    val a = razie.AA("lang", lang, "ok", ok, "css", css, "script", script)

    Draw.button(Sati(Pro.BASESVCNAME, razie.AI("view", "Fork"), a)) ::
      Nil :::
      (
        if ("scala" == lang)
          Draw.button(Sati("scripsterpro", razie.AI("prosession", "Try"), a)) :: Nil
        else
          Nil)
  }

  def j(lang: String, ok: String, k: String, ak: String) = "','" + lang + "', '" + ok + "', '" + k + "', '" + ak + "')"

  def mkCreate(lang: String, ok: String, k: String, api_key: String, script: String, css: String)(): Seq[NavLink] = {
    val a = razie.AA("lang", lang, "ok", ok, "k", k, "api_key", api_key, "script", script, "css", css)

    Draw.button(razie.AI("Link"), "javascript:scripsterJump('/cw/quote?kind=view" + j(lang, ok, k, api_key)) :: Nil :::
      Allow.embed(Draw.button(razie.AI("Embed read-only"), "javascript:scripsterJump('/cw/quote?kind=embed" + j(lang, ok, k, api_key))) :::
      (
        if ("scala" == lang)
          //            Draw.button(razie.AI("Link interactive"),   
          //                  "javascript:scripsterJump('/cw/quote?kind=interactive"+j(lang,ok, k, api_key)) ::
          Draw.button(razie.AI("Try now!"),
          "javascript:scripsterJump('/scripsterpro/prosession?toto=lots" + j(lang, ok, k, api_key)) ::
          Nil
        else Nil)
  }

  def mkFork(lang: String, ok: String, k: String, api_key: String, script: String, css: String)(): Seq[NavLink] = {
    val a = razie.AA("lang", lang, "ok", ok, "k", k, "api_key", api_key, "script", script, "css", css)

    Draw.button(razie.AI("Fork"), "javascript:scripsterJump('/cw/fork1?todo=lots" + j(lang, ok, k, api_key)) :: Nil :::
      (
        if ("scala" == lang)
          Draw.button(razie.AI("Try now!"),
          "javascript:scripsterJump('/scripsterpro/prosession?toto=lots" + j(lang, ok, k, api_key)) ::
          Nil
        else Nil)
  }

  @SoaMethod(descr = "exec a script", args = Array("lang", "ok", "k", "api_key", "css", "script"))
  def fork1(lang: String, ok: String, k: String, api_key: String, css: String, script: String) = {
    ApiKey.validate(api_key)
    val notice1 = Comms.readStream(this.getClass().getResource("/public/scripsterpro/scripsterpro-capture.html").openStream)
    val notice2 = Comms.readStream(this.getClass().getResource("/public/scripsterpro/scripsterpro-bottom.html").openStream)
    val formTitle = razie.AI("Capturester")
    val next = new Sati(Pro.BASESVCNAME, razie.AI("fork"), razie.AA("lang", lang, "api_key", api_key, "ok", ok, "script", Comms.encode(script)))

    Audit.recFork1(lang, ok, k, api_key, css, script)

    Draw seq (
      Draw html notice1,
      Draw.form(formTitle, next, razie.AA("css:String", "dark", "email:String", "anonymous Joe")).
      enumerated("css", "dark,light").labels(Pro.captureLabels).liner,
      Draw html notice2,
      Draw html Pro.blahblah)
  }

  @SoaMethod(descr = "exec a script", args = Array("lang", "ok", "api_key", "email", "css", "script"))
  def fork(lang: String, ok: String, api_key: String, email: String, css: String, script: String) = {
    ApiKey.validate(api_key)
    val k = Pro.generateKey(email)
    create(lang = lang, ok = ok, k = k, api_key = api_key, css = css, script = if (script == null) "" else Comms.decode(script))
  }

  /** this is the main entry point for the pro scripster */
  @SoaMethod(descr = "exec a script", args = Array("lang", "api_key", "email", "css", "kind", "script"))
  def capture(lang: String, api_key: String, email: String, css: String, kind: String, script: String) = {
    ApiKey.validate(api_key)
    val k = Pro.generateKey(email)
    create(lang = lang, ok = null, k = k, api_key = api_key, css = css, script = if (script == null) "" else Comms.decode(script))
  }

  @SoaMethod(descr = "exec a script")
  def register() {
    // TODO 
    Draw html "<p><font color=red>WELCOME - consider yourself registered! Kiddin' - use api_key=TEST for now...</font>"
  }

  // script is given when coming from scripster
  @SoaMethod(descr = "exec a script", args = Array("lang", "script"))
  @SoaMethodSink
  def start(_lang: String, script: String) = {
    val notice1 = Comms.readStream(this.getClass().getResource("/public/scripsterpro/scripsterpro-capture.html").openStream)
    val notice2 = Comms.readStream(this.getClass().getResource("/public/scripsterpro/scripsterpro-bottom.html").openStream)
    val formTitle = razie.AI("Capturester")
    val next = new Sati(Pro.BASESVCNAME, razie.AI("capture"), razie.AA("api_key", ApiKey.genForMe, "script", if (script == null) "" else Comms.encode(script)))

    Audit.recStart1()

    val lang = if (_lang == null || _lang == "") "scala" else _lang
    Draw seq (
      Draw html notice1,
      Draw.form(formTitle, next, razie.AA("lang:String", lang, "css:String", "dark", "email:String", "anonymous Joe")).
      enumerated("lang", Pro.langs).enumerated("css", "dark,light").labels(Pro.captureLabels).liner,
      Draw html notice2,
      Draw html Pro.blahblah,
      Draw html "<p>If you can't figure out when you'd use CodeWitter, read <a href=\"/cw/when\">this</>.")
  }

  // script is given when coming from scripster
  @SoaMethod(descr = "when to use it")
  def when() =
    Draw html Comms.readStream(this.getClass().getResource("/public/scripsterpro/when.html").openStream)

  @SoaMethod(descr = "exec a script", args = Array("kind", "lang", "ok", "k", "api_key", "script", "css"))
  def quote(kind: String, lang: String, ok: String, k: String, api_key: String, script: String, css: String) =
    Pro.quote(kind, lang, ok, k, api_key, script, css)
}

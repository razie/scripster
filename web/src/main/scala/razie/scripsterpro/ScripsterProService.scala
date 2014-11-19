/** ____    __    ____  ____  ____,,___     ____  __  __  ____
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

object Allow {
  def embed(d: NavLink): List[NavLink] = d::Nil // d
}

//TODO
object ApiKey {
  def validate(ak: String) {
    if (ak != "TEST")
      sys.error ("Opa! you need to register at http://cw.razie.com/cw/register")
  }

  def genForMe(): String = "TEST"

  def genFor(email: String): String = "TEST"
}

object Audit {
  private def from: String =
    Option(ExecutionContext.instance().a("httpattrs")).map(_.asInstanceOf[AttrAccess] sa "RemoteIP").getOrElse("x.x.x.x") +
      "/" +
      Option(ExecutionContext.instance().a("httpattrs")).map(_.asInstanceOf[AttrAccess] sa "X-Forwarded-For").getOrElse("x.x.x.x")

  private def audit(aa: AttrAccess) = razie.Audit (aa)

  def enc(s: String) = Comms.encode(s)

  def recTry(lang: String, orig_api_key: String, api_key: String, css: String, script: String) {
    audit (razie.AA("event", "new", "lang", lang, "orig_api_key", orig_api_key, "api_key", api_key, "css", css, "from", from, "script", enc(script)))
  }
}

/** statics for this service */
object Pro {
  lazy val notice = ScripsterService.resource ("/public/scripsterpro/scripster-view.html")
  lazy val blahblah = ScripsterService.resource ("/public/scripsterpro/scripsterpro-blahblah.html")

  def langs = "scala,java,javascript,xml,html,css,sparql,lua,php,plsql,python,sql,--------,whatever,English"

  def generateKey(email: String): String = {
    val pri = Comms.encode("a" + Hashability.hash (email))
    val pub = Comms.encode("a" + Hashability.hash (email + System.currentTimeMillis().toString))
    CodeWitterService.Audit.recHash (email, pub, pri)
    pub
  }

  /** this is the simple version, returning a string */
  def api_quote(kind: String, lang: String, ok: String, k: String, api_key: String, script: String, css: String) = {
    val a = razie.AA("lang", lang, "k", k, "css", css, "script", script)

    val target = Settings.target

    if (ok == null || ok.length <= 0)
      CodeWitterService.Audit.recNew (lang, k, api_key, css, script)
    else
      CodeWitterService.Audit.recFork (lang, ok, k, api_key, css, script)

    val ati = kind match {
      case "embed"       => new ServiceActionToInvoke(target, Pro.BASESVCNAME, razie.AI("embed"), a)
      case "view"        => new ServiceActionToInvoke(target, Pro.BASESVCNAME, razie.AI("view"), a)
      case "interactive" => new ServiceActionToInvoke(target, "scripsterpro", razie.AI("prosession"), a)
      case what @ _      => sys.error ("unkown kind of quote: " + kind)
    }

    // this one has to be absolute... there are some tricks when setting up the mutnat, so i'll use the pre-configured server
    val url = ati.makeActionUrl

    try {
      Draw toString Shortner.shorten(url)
    } catch {
      case e: Throwable =>
        Draw error (e.getMessage)
    }
  }

  /** this version returns a nice page */
  def quote(kind: String, lang: String, ok: String, k: String, api_key: String, script: String, css: String) = {
    val q = api_quote (kind, lang, ok, k, api_key, script, css)

    if (q.isInstanceOf[razie.draw.widgets.DrawError])
      q
    else {
      val shorty = q.toString

      // TODO read this - tips on reisinzg: http://guymal.com/mycode/iframe_size/
      val iframe = "<iframe src=\"" + shorty + "\" frameborder=0 width=\"80%\" ></iframe>"

      val scaladoc = ("[[" + shorty + " try now]]" :: "{{{" :: script.split('\n').toList ::: "}}}" :: Nil).map (" * " + _) mkString "\n"

      Draw seq (
        Draw link (AI(shorty), shorty),
        kind match {
          case "embed" => Draw seq (
            Draw html "<p><b>Embed is not that nice! <font color=yellow>Yet!</font></b>",
            Draw html "<p>You have to embed this manually ...<p>",
            Draw memo iframe,
            Draw html "<p>Looking like this: <p>",
            Draw html iframe)
          case "view" | "interactive" => Draw seq (
            Draw html "<p>You can copy the link and paste it to twitter or email it, or IM or...click the bird you like!<br>",
            // TODO collect stats which bird is clicked more
            Draw html "<a href=\"http://twitter.com/home?status=Checkout my witty code: " + Comms.encode(shorty) +
            "\" title=\"Click to send this page to Twitter!\" >" +
            "<img src=\"http://farm4.static.flickr.com/3331/3273076076_ff459b1f5e_o.png\" alt=\"Tweet This!\" />" +
            "</a>",
            Draw html "<a href=\"http://twitter.com/home?status=Checkout my witty code: " + Comms.encode(shorty) +
            "\" title=\"Click to send this page to Twitter!\" >" +
            "<img src=\"http://farm4.static.flickr.com/3526/3272256673_9693ef1ee1_o.png\" alt=\"Tweet This!\" />" +
            "</a>",
            Draw html "<p><p><b>Scaladoc copy/paste:</b><p>",
            Draw memo scaladoc)
          case _ => sys.error ("unkown kind of quote")
        })
    }
  }

  val BASESVCNAME = "cw"

  val captureLabels =
    AI ("lang", "Language") :: AI ("css", "Style") :: AI("email", "Email<em>*</em>") ::
      AI("submit", "continue") :: Nil
}

case class Sati(url:String, svc: String, ai: ActionItem, a: AttrAccess) extends ServiceActionToInvoke(url, svc, ai, a) {
  // TODO use some EMPTY constant
  // TODO add shortcuts for local execution 
  def this(svc: String, ai: ActionItem, a:AttrAccess) = this ("", svc, ai, a) 
  def this(svc: String, ai: ActionItem)               = this ("", svc, ai, new AttrAccessImpl()) 
}

/** the main scripster service (aka serlvet) */
@SoaService(name = "scripsterpro", descr = "Pro scripting service", bindings = Array("http"))
object ScripsterProService extends ScripsterProService

class ScripsterProService extends ScripsterService {
  /** this is the main entry point for the pro scripster */
  @SoaMethod(descr = "exec a script", args = Array("lang", "ok", "k", "api_key", "css", "script"))
  def prosession(lang: String, ok: String, k: String, api_key: String, css: String, script: String) = {
    Audit.recTry (lang, ok, api_key, css, script)

    val moreButtons =
      if (ok == null || ok.length <= 0)
        Draw.button(razie.AI("Link"), "javascript:scripsterJump('/cw/quote?kind=view" + CodeWitterService.j(lang, ok, k, api_key)) :: Nil
      else if (k == null || k.length <= 0)
        Draw.button(razie.AI("Fork"), "javascript:scripsterJump('/cw/fork1?todo=lots" + CodeWitterService.j(lang, ok, k, api_key)) :: Nil
      else
        Draw.button(razie.AI("Fork"), "javascript:scripsterJump('/cw/quote?kind=view" + CodeWitterService.j(lang, ok, k, api_key)) :: Nil

    val c = Sessions.create(Scripster.sharedContext, lang)
    val temp = mkPad (lang=lang, initial=script, sessionId=c.id, moreButtons=moreButtons)

    razie.Draw seq (
      mkTitle,
      temp,
      razie.Draw html "<p><b>Notice</b>",
      razie.Draw htmlMemo Comms.readStream (this.getClass().getResource("/public/scripster.html").openStream))
  }

  def d(orig: String, dflt: String) = if (orig == null || orig == "") dflt else orig

  @SoaMethod(descr = "exec a script")
  def tests() = {
    val cmd = razie.AI ("prosession")
    val ilang = "scala"
    val iapi_key = "akey"
    val iscript = "java.lang.System.prinln(\"gg\")"
    val icss = "dark"

    val SVC = "scripsterpro"

    Draw list (
      new ServiceActionToInvoke(SVC, razie.AI("test", "test dark"),
        razie.AA("lang", ilang, "api_key", iapi_key, "script", iscript, "css", "dark")),
      new ServiceActionToInvoke(SVC, razie.AI("test", "test light"),
        razie.AA("lang", ilang, "api_key", iapi_key, "script", iscript, "css", "light")),

      new ServiceActionToInvoke(SVC, razie.AI("test", "test js dark"),
        razie.AA("lang", "javascript", "api_key", iapi_key, "script", iscript, "css", "dark")),
      new ServiceActionToInvoke(SVC, razie.AI("test", "test js light"),
        razie.AA("lang", "javascript", "api_key", iapi_key, "script", iscript, "css", "light")))
  }

  @SoaMethod(descr = "exec a script", args = Array("lang", "api_key", "script", "css"))
  def test(lang: String, api_key: String, script: String, css: String) = {
    val cmd = razie.AI ("prosession")
    val ilang = d(lang, "scala")
    val iapi_key = d(api_key, "akey")
    val iscript = d(script, "java.lang.System.prinln(\"gg\")")
    val icss = d(css, "dark")

    Draw list (
      new ServiceActionToInvoke(Pro.BASESVCNAME, razie.AI("embed", "embed dark"),
        razie.AA("lang", ilang, "api_key", iapi_key, "script", iscript, "css", "dark")),
      new ServiceActionToInvoke(Pro.BASESVCNAME, razie.AI("embed", "embed light"),
        razie.AA("lang", ilang, "api_key", iapi_key, "script", iscript, "css", "light")),
      new ServiceActionToInvoke(Pro.BASESVCNAME, razie.AI("view"),
        razie.AA("lang", ilang, "api_key", iapi_key, "script", iscript, "css", icss)),
      new ServiceActionToInvoke("scripsterpro", razie.AI("test1"),
        razie.AA("lang", ilang, "api_key", iapi_key, "script", iscript, "css", icss)),
      new ServiceActionToInvoke("scripsterpro", razie.AI("test2"),
        razie.AA("lang", ilang, "api_key", iapi_key, "script", iscript, "css", icss)),
      new ServiceActionToInvoke("scripsterpro", razie.AI("use_cases"),
        razie.AA("lang", ilang, "api_key", iapi_key, "script", iscript, "css", icss)))
  }

  @SoaMethod(descr = "exec a script", args = Array("lang", "api_key", "script", "css"))
  def use_cases(lang: String, api_key: String, script: String, css: String) = {
    val cmd = razie.AI ("prosession")
    val ilang = d(lang, "scala")
    val iapi_key = d(api_key, "akey")
    val iscript = d(script, "java.lang.System.prinln(\"gg\")")
    val icss = d(css, "dark")

    Draw list (
      new ServiceActionToInvoke(Pro.BASESVCNAME, razie.AI("start", "share new script"),
        razie.AA("lang", ilang, "api_key", iapi_key, "script", iscript, "css", "dark")),
      new ServiceActionToInvoke(Pro.BASESVCNAME, razie.AI("embed", "view embedded"),
        razie.AA("lang", ilang, "api_key", iapi_key, "script", iscript, "css", "light")),
      new ServiceActionToInvoke(Pro.BASESVCNAME, razie.AI("view", "view linked - followed link"),
        razie.AA("lang", ilang, "api_key", iapi_key, "script", iscript, "css", icss)),
      new ServiceActionToInvoke(Pro.BASESVCNAME, razie.AI("try"),
        razie.AA("lang", ilang, "api_key", iapi_key, "script", iscript, "css", icss)))
  }

  @SoaMethod(descr = "exec a script")
  def test1() = {
    val link = "/scripsterpro/prosession?lang=scala&api_key=1&css=dark&initial=java.lang.System.prinln(\"gg\")"
    Draw seq (
      Draw html "<p> Before",
      Draw html "<iframe src=\"" + link + "\")\"/>",
      Draw html "<p> After",
      Draw html "<textarea id=\"code1\"></textarea><script ")
  }

  @SoaMethod(descr = "exec a script")
  def test2() = {
    val link = "/scripsterpro/prosession?lang=scala&api_key=1&css=dark&initial=java.lang.System.prinln(\"gg\")"
    Draw seq (
      Draw html "<p> Before",
      Draw html "<iframe src=\"" + link + "\")\"/>",
      Draw html "<p> After",
      Draw html "<textarea id=\"code1\"></textarea><script ")
  }

  private def soptions(sessionId: String)(s: String, pos:Int): Seq[String] = {
    Scripster.options(sessionId, s, pos).map (_.name)
  }
}

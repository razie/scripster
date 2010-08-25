/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
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
import razie.xp.XpJsonSolver
import razie.AI
import razie.scripster._
import razie.draw.widgets.NavLink

object Allow {
  def embed (d:NavLink) : List[NavLink] = Nil // d
}

//TODO
object ApiKey {
  def validate (ak:String) {
     if (ak != "TEST")
        error ("Opa! you need to register at http://cw.razie.com/cw/register")
  }

  def genForMe () : String = "TEST"
     
  def genFor (email:String) : String = "TEST"
}

object Audit {
  private def from : String = 
     Option(ExecutionContext.instance().a("httpattrs")).map(_.asInstanceOf[AttrAccess] sa "RemoteIP").getOrElse("x.x.x.x") +
     "/" +
     Option(ExecutionContext.instance().a("httpattrs")).map(_.asInstanceOf[AttrAccess] sa "X-Forwarded-For").getOrElse("x.x.x.x")

  private def audit (aa:AttrAccess) = razie.Audit (aa)

  def enc (s:String) = Comms.encode(s)
  
  def recNew  (lang:String, k:String, api_key:String, css:String, script:String) {
     audit (razie.AA("event","new", "lang",lang,"k",k,"api_key",api_key,"css",css,"from",from, "script",enc(script)))
  }
  def recCapture1  (lang:String, k:String, api_key:String, css:String, script:String) {
     audit (razie.AA("event","capture1", "lang",lang,"k",k,"api_key",api_key,"css",css,"from",from, "script",enc(script)))
  }
  def recView  (lang:String, k:String, css:String, script:String) {
     audit (razie.AA("event","view", "lang",lang,"k", k, "css",css,"from",from, "script", enc(script)))
  }
  def recEmbed  (lang:String, ok:String, css:String, script:String) {
     audit (razie.AA("event","embed", "lang",lang,"ok", ok, "css",css,"from",from, "script",enc(script)))
  }
  def recFork (lang:String, ok:String, k:String, api_key:String, css:String, script:String) {
     audit (razie.AA("event","fork", "lang",lang,"ok", ok, "k", k, "api_key",api_key,"css",css,"from",from,"script",enc(script)))
  }
  def recFork1 (lang:String, ok:String, k:String, api_key:String, css:String, script:String) {
     audit (razie.AA("event","fork1", "lang",lang,"ok", ok, "k", k, "api_key",api_key,"css",css,"from",from,"script",enc(script)))
  }
  def recTry  (lang:String, orig_api_key:String, api_key:String, css:String, script:String) {
     audit (razie.AA("event","new", "lang",lang,"orig_api_key", orig_api_key, "api_key",api_key,"css",css,"from",from,"script",enc(script)))
  }
  def recStart1  () {
     audit (razie.AA("event","start1", "from",from))
  }
  def recStart2  (lang:String, orig_api_key:String, api_key:String, css:String, script:String) {
     audit (razie.AA("event","start2", "lang",lang,"orig_api_key", orig_api_key, "api_key",api_key,"css",css,"from",from,"script",enc(script)))
  }
  
 def recHash  (email:String, pub_api_key:String, pri_api_key:String) {
     audit (razie.AA("event", "hash", "email", email, "pub_api_key",pub_api_key, "pri_api_key", pri_api_key, "from",from))
  }
  
}

/** statics for this service */
object Pro {
  def langs = "scala,java,javascript,xml,html,css,sparql,lua,php,plsql,python,sql,--------,whatever,English" 
    
  def generateKey (email:String) : String = {
    val pri = Comms.encode("a" + Hashability.hash (email))
    val pub = Comms.encode("a" + Hashability.hash (email+System.currentTimeMillis().toString))
    Audit.recHash (email, pub, pri)
    pub
  }
 
  /** this is the simple version, returning a string */
  def api_quote (kind:String, lang:String, ok:String, k:String, api_key:String, script:String, css:String) = {
    val a = razie.AA("lang", lang, "k", k, "css", css, "script", script)

    val target = Settings.target 
    
    if (ok == null || ok.length <= 0)
      Audit.recNew (lang, k, api_key, css, script)
    else
      Audit.recFork (lang, ok, k, api_key, css, script)
    
    val ati = kind match {
       case "embed" => new ServiceActionToInvoke (target, Pro.BASESVCNAME, razie.AI("embed"), a)
       case "view" => new ServiceActionToInvoke (target, Pro.BASESVCNAME, razie.AI("view"), a)
       case "interactive" => new ServiceActionToInvoke (target, "scripsterpro", razie.AI("prosession"), a)
       case _ => error ("unkown kind of quote")
    }

    // this one has to be absolute... there are some tricks when setting up the mutnat, so i'll use the pre-configured server
    val url = ati.makeActionUrl

    try {
    Draw toString Shortner.shorten(url)
     } catch { 
        case e:Throwable => 
          Draw error (e.getMessage)
     }
   }

  /** this version returns a nice page */
  def quote (kind:String, lang:String, ok:String, k:String, api_key:String, script:String, css:String) = {
    val q  = api_quote (kind, lang, ok, k, api_key, script, css)

    if (q.isInstanceOf[razie.draw.widgets.DrawError])
       q
    else {
    val shorty  = q.toString
    
    // TODO read this - tips on reisinzg: http://guymal.com/mycode/iframe_size/
    val iframe = "<iframe src=\""+ shorty + "\" frameborder=0 width=\"80%\" ></iframe>"
          
    Draw seq (
          Draw link (AI(shorty), shorty),
     kind match {
       case "embed" => Draw seq (
          Draw html "<p><b>Embed is not that nice! <font color=yellow>Yet!</font></b>",
          Draw html "<p>You have to embed this manually ...<p>",
          Draw memo iframe,
          Draw html "<p>Looking like this: <p>",
          Draw html iframe
                )
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
          		"</a>"
             )
       case _ => error ("unkown kind of quote")
    }
          )
    }
  }

  val BASESVCNAME = "cw"
     
  val captureLabels = 
     AI ("lang", "Language") :: AI ("css", "Style") :: AI("email", "Email<em>*</em>") ::  
     AI("submit", "continue") :: Nil
}

case class Sati (svc:String, ai:ActionItem, a:AttrAccess) extends ServiceActionToInvoke ("", svc, ai, a) {
   def this (svc:String, ai:ActionItem) = this (svc, ai, new AttrAccessImpl()) // TODO use some EMPTY constant
// TODO add shortcuts for local execution 
}
      
/** API CANNOT change - they will be exposed to public. see cw_api.html for exact API defn */
@SoaService (name="qw", descr="Pro scripting service", bindings=Array("http"))
object QuoteWitterService {
}

/** API CANNOT change - they will be exposed to public. see cw_api.html for exact API defn */
@SoaService (name="cw", descr="Pro scripting service", bindings=Array("http"))
object CodeWitterService {
   val cmdRSCRIPT = razie.AI ("run")
   val cmdRESET = razie.AI ("reset")
   
  /** simplest view - just the pad with links - it's a simple syntax-colored pad, view only  */
  @SoaMethod (descr="view a script", args=Array("lang", "ok", "css", "script"))
  def embed (lang:String, ok:String, css:String, script:String) = {
    val s = if (script==null)"" else script
    
    Audit.recEmbed (lang, ok, css, script)
      
    val lines = script.split('\n')
    val r = lines.size
    val c = script.filter(_=='\n').size + 1
    
    val p = new razie.draw.widgets.SimpleScriptPad (
          lang=lang,
          css=css,
          makeButtons=mkEmbed(lang, ok, css, script) _, 
          content=script,
          readOnly=true
          )
    // TODO i don't know why it doens't work with this on the line above
    p.rows(r)
    
    p
  }
   
  /** view a script - it's a simple syntax-colored pad, with some text, view only */
  @SoaMethod (descr="view a script", args=Array("lang", "k", "css", "script"))
  def view (lang:String, k:String, css:String, script:String) = {
    val notice = Comms.readStream (this.getClass().getResource("/public/scripsterpro/scripster-view.html").openStream)
    val blahblah = Comms.readStream (this.getClass().getResource("/public/scripsterpro/scripsterpro-blahblah.html").openStream)
    
    Audit.recView (lang, k, css, script)
      
    val p = new razie.draw.widgets.SimpleScriptPad (
          lang=lang,
          css=css,
          makeButtons=mkFork(lang, k, "", ApiKey.genForMe, css, script) _, 
          content=script
          )
    
    razie.Draw seq ( 
      razie.Draw html "<p><b>Code Witter - tweet your witty code</b>",
      p,
      razie.Draw htmlMemo notice,
      razie.Draw htmlMemo blahblah)
  }
   
  /** view a script - it's a simple syntax-colored pad, with some text, view only */
  @SoaMethod (descr="view a script", args=Array("lang", "ok", "k", "api_key", "css", "script"))
  def create (lang:String, ok:String, k:String, api_key:String, css:String, script:String) = {
    val notice = Comms.readStream (this.getClass().getResource("/public/scripsterpro/scripster-view.html").openStream)
    val blahblah = Comms.readStream (this.getClass().getResource("/public/scripsterpro/scripsterpro-blahblah.html").openStream)
    
    Audit.recCapture1 (lang, k, api_key, css, script)
    
    val p = new razie.draw.widgets.SimpleScriptPad (
          lang=lang,
          css=css,
          makeButtons=mkCreate(lang, (if(ok==null) "" else ok), k, api_key, script, css) _, 
          content=script
          )
    
    razie.Draw seq ( 
      razie.Draw html "<p><b>Code Witter - tweet your witty code</b>",
      p,
      razie.Draw htmlMemo notice,
      razie.Draw htmlMemo blahblah)
  }
   
  def mkEmbed (lang:String, ok:String, css:String, script:String)() : Seq[NavLink] = {
    val a = razie.AA("lang", lang, "ok", ok, "css", css, "script", script)
    
    Draw.button(Sati (Pro.BASESVCNAME, razie.AI("view", "Fork"), a)) :: 
    Nil :::
    (
          if ("scala" == lang) 
             Draw.button(Sati ("scripsterpro", razie.AI("prosession", "Try"), a)) :: Nil
          else 
             Nil
    ) 
  }
 
  def j(lang:String, ok:String, k:String, ak:String) = "','"+lang+"', '"+ok+"', '"+k+"', '"+ak+"')"
     
  def mkCreate (lang:String, ok:String, k:String, api_key:String, script:String, css:String)() : Seq[NavLink] = {
    val a = razie.AA("lang", lang, "ok", ok, "k", k, "api_key", api_key, "script", script, "css", css)
   
    Draw.button(razie.AI("Link"),   "javascript:scripsterJump('/cw/quote?kind=view"+j(lang,ok, k, api_key)) :: Nil :::
    Allow.embed(Draw.button(razie.AI("Embed read-only"), "javascript:scripsterJump('/cw/quote?kind=embed"+j(lang,ok, k, api_key))) :::
    (
          if ("scala" == lang) 
//            Draw.button(razie.AI("Link interactive"),   
//                  "javascript:scripsterJump('/cw/quote?kind=interactive"+j(lang,ok, k, api_key)) ::
            Draw.button(razie.AI("Try now!"),   
                  "javascript:scripsterJump('/scripsterpro/prosession?toto=lots"+j(lang,ok, k, api_key)) ::
             Nil
          else Nil
    ) 
  }

  def mkFork (lang:String, ok:String, k:String, api_key:String, script:String, css:String)() : Seq[NavLink] = {
    val a = razie.AA("lang", lang, "ok", ok, "k", k, "api_key", api_key, "script", script, "css", css)

    Draw.button(razie.AI("Fork"),   "javascript:scripsterJump('/cw/fork1?todo=lots"+j(lang,ok, k, api_key)) :: Nil :::
    (
          if ("scala" == lang) 
            Draw.button(razie.AI("Try now!"),   
                  "javascript:scripsterJump('/scripsterpro/prosession?toto=lots"+j(lang,ok, k, api_key)) ::
             Nil
          else Nil
    ) 
  }

  @SoaMethod (descr="exec a script", args=Array("lang", "ok", "k", "api_key", "css", "script"))
  def fork1 (lang:String, ok:String, k:String, api_key:String, css:String, script:String) = {
     ApiKey.validate(api_key)
    val notice1 = Comms.readStream (this.getClass().getResource("/public/scripsterpro/scripsterpro-capture.html").openStream)
    val notice2 = Comms.readStream (this.getClass().getResource("/public/scripsterpro/scripsterpro-bottom.html").openStream)
    val blahblah = Comms.readStream (this.getClass().getResource("/public/scripsterpro/scripsterpro-blahblah.html").openStream)
    val formTitle = razie.AI ("Capturester")
    val next = new Sati(Pro.BASESVCNAME, razie.AI ("fork"), razie.AA("lang", lang, "api_key", api_key, "ok", ok, "script", Comms.encode(script)))
    
    Audit.recFork1 (lang,ok, k, api_key,css, script)
    
    Draw seq (
      Draw html notice1,
      Draw.form(formTitle, next, razie.AA ("css:String", "dark", "email:String","anonymous Joe")).
         enumerated("css", "dark,light").labels(Pro.captureLabels).liner,
      Draw html notice2,
      Draw html blahblah
      )
}

  @SoaMethod (descr="exec a script", args=Array("lang", "ok", "api_key", "email", "css", "script"))
  def fork (lang:String, ok:String, api_key:String, email:String, css:String, script:String) = {
     ApiKey.validate(api_key)
     val k = Pro.generateKey (email)
     create (lang=lang, ok=ok, k=k, api_key=api_key, css=css, script=if (script==null)"" else Comms.decode(script))
  }


  /** this is the main entry point for the pro scripster */
  @SoaMethod (descr="exec a script", args=Array("lang", "api_key", "email", "css", "kind", "script"))
  def capture (lang:String, api_key:String, email:String, css:String, kind:String, script:String) = {
     ApiKey.validate(api_key)
     val k = Pro.generateKey (email)
     create (lang=lang, ok=null, k=k, api_key=api_key, css=css, script=if (script==null)"" else Comms.decode(script))
  }

  @SoaMethod (descr="exec a script")
  def register () {
     // TODO 
    Draw html "<p><font color=red>WELCOME - consider yourself registered! Kiddin' - use api_key=TEST for now...</font>"
  }

  // script is given when coming from scripster
  @SoaMethod (descr="exec a script", args=Array("lang", "script"))
  @SoaMethodSink
  def start (lang:String, script:String) = {
    val notice1 = Comms.readStream (this.getClass().getResource("/public/scripsterpro/scripsterpro-capture.html").openStream)
    val notice2 = Comms.readStream (this.getClass().getResource("/public/scripsterpro/scripsterpro-bottom.html").openStream)
    val blahblah = Comms.readStream (this.getClass().getResource("/public/scripsterpro/scripsterpro-blahblah.html").openStream)
    val formTitle = razie.AI ("Capturester")
    val next = new Sati(Pro.BASESVCNAME, razie.AI ("capture"), razie.AA("api_key", ApiKey.genForMe, "script", if(script==null)"" else Comms.encode(script)))
    
    Audit.recStart1 ()
    
    Draw seq (
      Draw html notice1,
      Draw.form(formTitle, next, razie.AA ("lang:String", lang, "css:String", "dark", "email:String","anonymous Joe")).
         enumerated("lang", Pro.langs).enumerated ("css", "dark,light").labels(Pro.captureLabels).liner,
      Draw html notice2,
      Draw html blahblah,
      Draw html "<p>If you can't figure out when you'd use CodeWitter, read <a href=\"/cw/when\">this</>."
      )
  }
  
  // script is given when coming from scripster
  @SoaMethod (descr="exec a script")
  def when () = 
    Draw html Comms.readStream (this.getClass().getResource("/public/scripsterpro/when.html").openStream)
  
  @SoaMethod (descr="exec a script", args=Array("kind", "lang", "ok", "k", "api_key", "script", "css"))
  def quote (kind:String, lang:String, ok:String, k:String, api_key:String, script:String, css:String) = 
    Pro.quote(kind, lang, ok, k, api_key, script, css)

}

//--------------------------- the old one
//--------------------------- the old one
//--------------------------- the old one

/** API CANNOT change - they will be exposed to public */
@SoaService (name="sharescript", descr="Pro scripting service", bindings=Array("http"))
object ShareScriptService {
   val cmdRSCRIPT = razie.AI ("run")
   val cmdRESET = razie.AI ("reset")
   
  /** simplest view - just the pad with links - it's a simple syntax-colored pad, view only  */
  @SoaMethod (descr="view a script", args=Array("lang", "api_key", "script", "css"))
  def embed (lang:String, api_key:String, script:String, css:String) = {
    Draw seq (
     CodeWitterService.embed(lang, api_key, css, script),
     Draw html "<p><font color=red>OBSOLETE - please re-create this link. It will not work anymore, shortly</font>"
     )
  }
   
  /** view a script - it's a simple syntax-colored pad, with some text, view only */
  @SoaMethod (descr="view a script", args=Array("lang", "orig_api_key", "api_key", "script", "css"))
  def view (lang:String, orig_api_key:String, api_key:String, script:String, css:String) = {
    Draw seq (
     CodeWitterService.view(lang, api_key, css, script),
     Draw html "<p><font color=red>OBSOLETE - please re-create this link. It will not work anymore, shortly</font>"
     )
  }
  
  @SoaMethod (descr="exec a script")
  @SoaMethodSink
  def start () = 
    Draw html "<p><font color=red>OBSOLETE - update your links to:.</font> <a href=\"/cw/start\">/cw/start</a>"
}

/** the main scripster service (aka serlvet) */
@SoaService (name="scripsterpro", descr="Pro scripting service", bindings=Array("http"))
object ScripsterProService {
   val cmdRSCRIPT = razie.AI ("run")
   val cmdRESET = razie.AI ("reset")
   
  @SoaMethod (descr="interactive", args=Array("sessionId", "line"))
  def options (sessionId:String, line:String) = 
    ScripsterService.options(sessionId, line)
  
  @SoaMethod (descr="exec a script", args=Array("sessionId", "language", "script"))
  def run (sessionId:String, language:String, script:String) = {
    Draw toString ScripsterService.run(sessionId, language, script)
  }
  
  @SoaMethod (descr="create a new session and a simple pad", args=Array("lang", "initial"))
  def pad (lang:String, initial:String = null) = {
    val c = Sessions.create(Scripster.sharedContext, lang)
    if (initial == null || initial == "")
      new razie.draw.widgets.ScriptPad (lang=lang,run=mkATI(c.id) _, options=soptions(c.id) _, reset=mkRESET(c.id) _)
    else
      new razie.draw.widgets.ScriptPad (lang=lang,run=mkATI(c.id) _, options=soptions(c.id) _, reset=mkRESET(c.id) _, initial=initial)
  }

  @SoaMethod (descr="exec a script", args=Array("lang", "initial"))
  def session (lang:String, initial:String) = {
    val notice = Comms.readStream (this.getClass().getResource("/public/scripster.html").openStream)
    val title = razie.Draw html "<b> Scripster - interactive scala script pad</b>"
    
    if (notice != null && notice.length > 0 ) 
       Draw seq ( title, pad(lang), razie.Draw html "<p><b>Notice</b>", razie.Draw htmlMemo notice)
    else 
       Draw seq (title, pad(lang))
  }

  /** this is the main entry point for the pro scripster */
  @SoaMethod (descr="exec a script", args=Array("lang", "ok", "k", "api_key", "css", "script"))
  def prosession (lang:String, ok:String, k:String, api_key:String, css:String, script:String) = {
    Audit.recTry (lang, ok, api_key, css, script)
    
    val p = pad(lang, script)
    p.moreButtons = 
      if (ok == null || ok.length <=0) 
        Draw.button(razie.AI("Link"),   "javascript:scripsterJump('/cw/quote?kind=view"+CodeWitterService.j(lang,ok, k, api_key)) :: Nil 
      else if (k == null || k.length <= 0)
        Draw.button(razie.AI("Fork"),   "javascript:scripsterJump('/cw/fork1?todo=lots"+CodeWitterService.j(lang,ok, k, api_key)) :: Nil
      else
        Draw.button(razie.AI("Fork"),   "javascript:scripsterJump('/cw/quote?kind=view"+CodeWitterService.j(lang,ok, k, api_key)) :: Nil 
       
    razie.Draw seq ( 
      razie.Draw html "<b> Scripster - interactive scala scripting</b>",
      p,
      razie.Draw html "<p><b>Notice</b>", 
      razie.Draw htmlMemo Comms.readStream (this.getClass().getResource("/public/scripster.html").openStream)
      )
  }

  def d (orig:String, dflt:String) = if (orig == null || orig == "") dflt else orig
  
  @SoaMethod (descr="exec a script")
  def tests () = {
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
          razie.AA("lang", "javascript", "api_key", iapi_key, "script", iscript, "css", "light"))
      )
  }
  
  @SoaMethod (descr="exec a script", args=Array("lang", "api_key", "script", "css"))
  def test (lang:String, api_key:String, script:String, css:String) = {
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
          razie.AA("lang", ilang, "api_key", iapi_key, "script", iscript, "css", icss))
      )
  }
  
  @SoaMethod (descr="exec a script", args=Array("lang", "api_key", "script", "css"))
  def use_cases (lang:String, api_key:String, script:String, css:String) = {
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
          razie.AA("lang", ilang, "api_key", iapi_key, "script", iscript, "css", icss))
      )
  }
  
  @SoaMethod (descr="exec a script")
  def test1 () = {
     val link = "/scripsterpro/prosession?lang=scala&api_key=1&css=dark&initial=java.lang.System.prinln(\"gg\")"
     Draw seq (
           Draw html "<p> Before",
           Draw html "<iframe src=\"" + link + "\")\"/>",
           Draw html "<p> After",
           Draw html "<textarea id=\"code1\"></textarea><script "
           )
  }
  
  @SoaMethod (descr="exec a script")
  def test2 () = {
     val link = "/scripsterpro/prosession?lang=scala&api_key=1&css=dark&initial=java.lang.System.prinln(\"gg\")"
     Draw seq (
           Draw html "<p> Before",
           Draw html "<iframe src=\"" + link + "\")\"/>",
           Draw html "<p> After",
           Draw html "<textarea id=\"code1\"></textarea><script "
           )
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

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

/** simple session manager - sessions expire in 7 minutes */
object Sessions {
  val life = intprop("scripster.sessions.life", 3 * 60) * 1000L // 2 minutes
  val max = intprop("scripster.sessions.max", 12) // 2 minutes
  val map = razie.Mapi[String, ScriptSession] ()

  def intprop(prop: String, dflt: Int): Int = {
    val s = System.getProperty(prop)

    try {
      Integer.parseInt(s)
    } catch {
      case e: Throwable => dflt
    }
  }

  def create(parent: ScriptContext, lang: String) = {
    clean
    if (map.size >= max) {
      val now = System.currentTimeMillis
      Audit.recSessionFail (lang)
      sys.error ("too many sessions - come back...later, sessions=" +
        map.size +
        "  - patience, one expires in: " + (life - (now - (map.values.map(_.time).foldRight(now)(
          (x, y) => if (x < y) x else y)))) / (1000L) + "sec")
    }

    Audit.recSession (lang)
    val c = new ScriptSession(parent, lang)
    map.put(c.id, c)
    c
  }

  def get(key: String) = {
    val c = map.get(key)
    c.foreach(_.time = System.currentTimeMillis) // reset last access
    c
  }

  def clean {
    val time = System.currentTimeMillis
    val oldies = map.values.filter(_.time < time - life).toList
    oldies.foreach(x => map.remove(x.id))
  }
}

/** Sessions of scripting - maintain state */
class ScriptSession(parent: ScriptContext, val lang: String) {
  var time = System.currentTimeMillis
  val id = time.toString // TODO use GUID
  val ctx = new ScalaScriptContext(parent)
  var buffer = new StringBuilder()
  var pcount = 0

  def script = {
    var s = buffer.toString
    // TODO idiot code but I'm bored of this right now, want to play piano instead
    if (s.indexOf("{") == 0 && s.lastIndexOf("}") == s.length - 1)
      s substring (1, s.length - 1)
    else
      s
  }

  // accumulate and identify statement blocks
  def accumulate(more: String) = {
    for (c <- more; if c == '{') pcount += 1
    for (c <- more; if c == '}') pcount -= 1
    buffer append more
    if (pcount == 0) buffer append "\n" // must treat like a line
  }

  def inStatement = pcount > 0

  def clear { pcount = 0; buffer = new StringBuilder() }
}

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

//// TODO find/write DB 
//trait NewShortner[T]  {
//  def shorten  (in:T) : String
//  def retrieve (shorted:String) : T
//}

object Shortner {
  def shorten(in: String): String = Settings.shorty.shorten(in)
}

trait Shortner {
  def shorten(in: String): String
}

class Dummy extends Shortner {
  override def shorten(in: String): String = in
}

class Bitly extends Shortner with razie.Logging {
  override def shorten(in: String): String = {
    try {
      val enc = Comms.encode(in)
      val bitly = "http://api.bit.ly/shorten?version=2.0.1" +
        "&login=razie&apiKey=" + Settings.bitlykey +
        "&longUrl=" + enc

      val bitret = Comms.readUrl(bitly)
      val j = razie.Snakk.json(bitret)

      debug("bit.ly reply to " + bitly + " \n was \n" + bitret)

      val ec = j \@@ "errorCode"
      try {
        val ret = if ((j \@@ "errorCode") == "0") { // ok...
          j \@@ "*/*/*/@shortUrl"
        } else { // this shouldnb't actually hapen, eh?
          in
        }
        ret
      } catch {
        case e: Throwable =>
          log("ERROR_BITLY: ", e)
          sys.error("ERROR_BITLY: " + ec)
      }
    } catch {
      case e: Throwable =>
        log("ERROR_BITLY: ", e)
        sys.error("ERROR_BITLY: no connection/reply")
    }
  }
}

// to run this set the bit.ly key -Dscripsterpro.bitlykey=XXXXX
object CheckBitly extends App {
  val x = new Bitly().shorten("http://www.razie.com")
  println(x)
}

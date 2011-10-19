package razie.scripster.test

import org.junit.Test
import razie.base.scripting._
import razie.scripster._
import org.scalatest.matchers.MustMatchers
import com.razie.pub.comms.ServiceActionToInvoke
import com.razie.pub.comms.AgentHandle
import com.razie.pub.base.NoStatics
import com.razie.pub.comms.Agents
import com.razie.pub.comms.AgentCloud
import org.junit.BeforeClass
import org.junit.Before
import razie.Snakk

class PerfLocalhostTest extends MustMatchers {

  @Before def mysetup { mysetupv }
  lazy val mysetupv = {
    val ME = new AgentHandle("localhost", "localhost", "127.0.0.1", 4445
      .toString(), "http://localhost:" + 4445.toString());
    NoStatics.put(classOf[Agents], new Agents(new AgentCloud(ME), ME));
    4
  }

  val URL = "http://localhost:4445"
//  val URL = "http://www.tryscala.com"
    
  def create = new ServiceActionToInvoke(URL, "scripster", razie.AI("apisessioncreate", "apisessioncreate"),
    razie.AA("lang", "javascript", "api_key", "kk", "script", "1+2", "css", "dark"))
  def apirun(session: String) = new ServiceActionToInvoke(URL, "scripster", razie.AI("apirun", "apirun"),
    razie.AA("language", "scala", "sessionId", session, "script", "1+2"))
  def options(session: String) = new ServiceActionToInvoke(URL, "scripster", razie.AI("options", "options"),
    razie.AA("language", "scala", "sessionId", session, "line", "1.", "pos", 2.toString))
  def close(session: String) = new ServiceActionToInvoke(URL, "scripster", razie.AI("apisessionclose", "apisessionclose"),
    razie.AA("language", "scala", "sessionId", session, "script", "1+2"))

  val BIGL = 1
  val THREADS = Sessions.max-1
  val SMALL = 10
  
  var curr = 0
  val start=System.currentTimeMillis()

  @Test def testsimple100 = expect (3 * BIGL * THREADS * SMALL) {
    var e: Throwable = null

    val res = (for (i <- 0 until BIGL) yield {
      val ls = razie.Threads.forkjoin(0 until THREADS) { x: Int =>
        (try {
          for (i <- 0 until SMALL) yield {
            val s = Snakk.body (Snakk.url(create.makeActionUrl()))
            println ("session: "+s)
            val o = Snakk.htmlBody (Snakk.url(options(s).makeActionUrl()))
            println ("options: "+o)
            val r = Snakk.body (Snakk.url(apirun(s).makeActionUrl()))
            synchronized { curr += 1 }
            println ("session "+curr+" result: "+r)
            val res = Snakk.htmlBody (Snakk.url(close(s).makeActionUrl()))
            r.toString.toInt
          }
        } catch { case s @ _ if (e != null) => { e = s; Seq(0) } }).sum

      }

      if (e != null) throw e

      (for (x <- ls; s <- x) yield s).sum
    }).sum
    
    println ("WE DID: "+BIGL*THREADS*SMALL+" sessions... "+((3*BIGL*THREADS*SMALL - res)/3)+" FAILED!")
    val end=System.currentTimeMillis()
    println ("Performance: %.2f sessions per second.".format((BIGL*THREADS*SMALL)*1000.0/(end-start)))
    res
  }

}

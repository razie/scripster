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
import razie.scripster._

object MainScripsterPro extends App {
  // warm up the interpreter while you move your hands... :)
  new java.lang.Thread(
    new java.lang.Runnable {
      def run() {
        new ScalaScript("1+2").eval(ScriptContextImpl.global)
      }
    }).start()

  Scripster.createServer(4445, services =
    new HttpSoaBinding(CodeWitterService) :: Nil :::
      (
        if (System.getProperty("scripsterpro.run", "true").equals("true"))
          new HttpSoaBinding(ScripsterProService) :: Nil
        else Nil))

}


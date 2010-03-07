/**  ____    __    ____  ____  ____/___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___) __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__)\__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)___/   (__)  (______)(____/   LICENESE.txt
 */
package razie.base.scripting

import com.razie.pub.base._

/** add capability to support scala scripts */
class ScriptFactoryScala (val other:ScriptFactory, val dflt:Boolean) extends ScriptFactory {

   override def makeImpl (lang:String, s:String) = {
      (lang, dflt) match {
         case ("scala", _) | (null, true) => new ScriptScala(s)
         case _ => other.makeImpl (lang, s)
      }
   }
}

/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.scripting

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

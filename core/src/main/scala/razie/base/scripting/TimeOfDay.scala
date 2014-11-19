/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting;

import java.util.Date;
import razie.base._

/**
 * easy to use scriptable for date-time related stuff. I use it to play with scripting and presentation etc
 * 
 * @author razvanc
 */
class TimeOfDay {

  if (aivalues == null)
    aivalues = {
      for (i <- 0 to values.length)
        yield values()(i)
    }.toArray

  def tags() = TimeOfDay.TAGS

  def values() = aivalues

  def value() = {
    val dt = new Date()
    RangeSel.rangeSel(dt.getHours(), TimeOfDay.rangei, TimeOfDay.values, dflt);
  }

  def aivalue() = {
    val dt = new Date()
    RangeSel.rangeSel(dt.getHours(), TimeOfDay.rangei, aivalues, aidflt);
  }

  val dflt = "?";

  var aivalues: Array[ActionItem] = null
  val aidflt = new ActionItem("?");

}

object TimeOfDay {

  //    def calcvalue() : String = {new TimeOfDay().value();}

  val TAGS = Array("time")

  val rangei: Array[Array[Int]] = Array(Array(0, 6), Array(7, 11), Array(12, 19), Array(20, 23))
  val values = Array("night", "morning", "day", "evening")

  val ranges: Array[Array[String]] = Array(Array("0", "6", "night"), Array("7", "11", "morning"), Array("12", "18", "day"),
    Array("19", "23", "evening"))
}

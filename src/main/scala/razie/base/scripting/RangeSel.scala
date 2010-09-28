/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting;


/**
 * simple class to map a value from a range 
 * 
 * @author razvanc
 */
object RangeSel {
  def rangeSel[T] (hours:Int, range:Array[Array[Int]], values:Array[T], dflt:T) : T = {
    for (i <- 0 until range.length)
       if (hours >= range(i)(0) && hours <= range(i)(1))
          return values(i);
    return dflt;
    }

}

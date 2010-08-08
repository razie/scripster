/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting;


/**
 * TODO 1-1 detailed docs
 * 
 * @author razvanc
 */
class RangeSel[T] {

  def rangeSel(hours:Int, range:Array[Array[Int]], values:Array[T], dflt:T) : T = {
    for (i <- 0 until range.length)
       if (hours >= range(i)(0) && hours <= range(i)(1))
          return values(i);
    return dflt;
    }

}

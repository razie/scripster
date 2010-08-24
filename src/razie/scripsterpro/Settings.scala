/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.scripsterpro

import com.razie.pub.comms._

object Settings {
  val testing = razie.Boolean(System.getProperty("scripsterpro.testing", "false"))
 
  def nok[A] (a:A, b:A) = if (! testing) a else b
  
  lazy val target = nok (
    System.getProperty("scripsterpro.target.url", Agents.me().url),
    Agents.me().url
    )
  
  val shorty : Shortner = nok (
    new Bitly()   ,
    new Dummy()   
    )
}

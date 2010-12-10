/* NSC -- new Scala compiler
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Martin Odersky
 */

package scala.tools.nsc

trait RAZIEInterpreter {
  
   // BEGIN RAZ hacks

   // 1. Request is private. I need: dependencies (usedNames?) newly defined values (boundNames?)
   // the resulting value and the error message(s) if any
   case class PublicRequest (usedNames : List[String], valueNames:List[String], extractionValue:Option[Any], err:List[String])

   // 2. Can't get the last request
   def lastRequest : Option[PublicRequest] 
   // 3. Can't get the actual error message
   // TODO this needs cleared after every run...ugly - no time to fix. should group lastErr under lastRequ
   var errAccumulator : scala.collection.mutable.ListBuffer[String]

}
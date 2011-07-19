package razie.base.scripting

import razie.base.ActionContext
import scala.tools.{ nsc => nsc }
import scala.tools.nsc.{ InterpreterResults => IR }

/** hacking the scala interpreter - this will accumulate errors and retrieve all new defined values */
class RazieInterpreterImpl(s: nsc.Settings) extends nsc.Interpreter(s) with nsc.RAZIEInterpreter {
  import memberHandlers._
//  import nsc.interpreter.MemberHandlers
//  this: nsc.Interpreter with nsc.RAZIEInterpreter

   // 1. Request is private. I need: dependencies (usedNames?) newly defined values (boundNames?)
   // the resulting value and the error message(s) if any
   // 2. Can't get the last request
   def lastRequest : Option[PublicRequest] =
     prevRequestList.lastOption map (l =>
//       PublicRequest (l.usedNames.map(_.decode), l.valueNames.map(_.decode), l.extractionValue, errAccumulator.toList)
       PublicRequest (
           l.referencedNames.map(_.decode), 
           l.handlers.collect { case x: ValHandler => x.name }.map(_.decode), 
           try l.getEval catch { case _ => None} , //l.extractionValue,  // TODO hides some errors
           errAccumulator.toList)
    
       )
   
//  def allImplicits                   = allHandlers filter (_.definesImplicit) flatMap (_.definedNames)
//  def importHandlers                 = allHandlers collect { case x: ImportHandler => x }

//  /** hacked reporter - accumulates erorrs... */
//  class MyPrintWriter extends nsc.NewLinePrintWriter(new ConsoleWriter, true) {
//    override def println() { print("\n"); flush() }
//  }

  def evalExpr[T](code:String): T = {
 beQuietDuring {
    interpret(code) match {
      case IR.Success =>
        try lastRequest.flatMap (_.extractionValue).get.asInstanceOf[T]
        catch { case e: Exception => out println e ; throw e }
      case _ => throw new IllegalStateException ("parser didn't return success")
    }
  }  
 }
  
  def eval(s: ScalaScript, ctx: ActionContext): RazScript.RSResult[Any] = {
    beQuietDuring {
      interpret(s.script) match {
        case IR.Success =>
          if (lastRequest map (_.extractionValue.isDefined) getOrElse false)
            RazScript.RSSucc(lastRequest.get.extractionValue get)
          else
            RazScript.RSSuccNoValue
        case IR.Error => {
          val c =
            if (lastRequest.get.valueNames.contains("lastException"))
              RazScript.RSError(evalExpr[Exception]("lastException").getMessage)
            else RazScript.RSError(lastRequest.get.err mkString "\n\r")
          errAccumulator.clear
          c
        }
        case IR.Incomplete => RazScript.RSIncomplete
      }
    }
  }

  def lastNames = {
    // TODO nicer way to build a map from a list?
    val ret = new scala.collection.mutable.HashMap[String, Any]()
    // TODO get the value of x nicer
    for (
      x <- lastRequest.get.valueNames if (x != "lastException" && !x.startsWith ("synthvar$") && x != "ctx")
    ) {
      val xx = (x -> evalExpr[Any](x))
      razie.Debug("bound: " + xx)
      //      ret += (x -> evalExpr[Any](x))
      ret += xx
    }
    ret
  }
}

/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting

import scala.tools.nsc
import scala.tools.nsc.interpreter.IR
import scala.tools.nsc.interpreter.ReplReporter

/** hacking the scala interpreter - this will accumulate errors and retrieve all new defined values */
class RazieInterpreter(s: nsc.Settings) extends nsc.Interpreter(s) {
import memberHandlers._

  // What Razie needs in Request
  case class PublicRequest(usedNames: List[String], valueNames: List[String], extractionValue: Option[Any], err: List[String])

  def razlastRequest: Option[PublicRequest] =
    prevRequestList.lastOption map (l =>
      PublicRequest(
        l.referencedNames.map(_.decode),
        l.handlers.collect { case x: ValHandler => x.name }.map(_.decode),
        try l.lineRep.callOpt("$result") catch { case _ => None }, //l.extractionValue,  // TODO hides some errors
//worked in 2.9.0-1        try l.getEval catch { case _ => None }, // TODO hides some errors
        errAccumulator.toList))

  // TODO this needs cleared after every run...ugly - no time to fix. should group lastErr under lastRequ
  private var errAccumulator = new scala.collection.mutable.ListBuffer[String]()

  /** hacked reporter - accumulates erorrs... */
  lazy override val reporter = new ReplReporter(this) {
    override def printMessage(msg: String) {
      errAccumulator append msg
      out println msg
      //      out println clean(msg)
      out.flush()
    }
  }

  /** evauate an expression and get the result */
  def evalExpr[T](code: String): T = {
    beQuietDuring {
      interpret(code) match {
        case IR.Success =>
          try razlastRequest.flatMap(_.extractionValue).get.asInstanceOf[T]
          catch { case e: Exception => out println e; throw e }
        case IR.Error => {
          val c =
            if (razlastRequest.get.valueNames.contains("lastException"))
              RazScript.RSError(evalExpr[Exception]("lastException").getMessage)
            else RazScript.RSError(razlastRequest.get.err mkString "\n\r")
          errAccumulator.clear
          throw new IllegalStateException(c.err)
        }
        case _ => throw new IllegalStateException("parser didn't return success")
      }
    }
  }

  /** evaluate a script */
  def eval(s: ScalaScript): RazScript.RSResult[Any] = {
    beQuietDuring {
      interpret(s.script) match {
        case IR.Success =>
          if (razlastRequest map (_.extractionValue.isDefined) getOrElse false)
            RazScript.RSSucc(razlastRequest.get.extractionValue get)
          else
            RazScript.RSSuccNoValue
        case IR.Error => {
          val c =
            if (razlastRequest.get.valueNames.contains("lastException"))
              RazScript.RSError(evalExpr[Exception]("lastException").getMessage)
            else RazScript.RSError(razlastRequest.get.err mkString "\n\r")
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
      x <- razlastRequest.get.valueNames if (x != "lastException" && !x.startsWith("synthvar$") && x != "ctx")
    ) {
      val xx = (x -> evalExpr[Any](x))
      razie.Debug("bound: " + xx)
      //      ret += (x -> evalExpr[Any](x))
      ret += xx
    }
    ret
  }

}

object Main34 extends App {
  val ctx = ScalaScriptContext("a" -> 1)//, "b" -> 2)
  val res = ScalaScript("val c = a+b").eval(ctx) match {
    case RazScript.RSError(msg) => msg
    case _ => ""
  }
  println(res)
  res
  
  val pr = ("xxxxxxxxxxinterpret" + (for (i <- (0 until 5).toList) yield (razie.Timer {
    println("yyy " + ctx.parser.interpret("1+2"))
    })).mkString("\n")+"\n") ::
  ("xxxxxxxxxxevalExpr" + (for (i <- (0 until 5).toList) yield (razie.Timer {
    ctx.parser.evalExpr[Any]("1+2")
    })).mkString("\n")+"\n") ::
  ("xxxxxxxxxxeval" + (for (i <- (0 until 5).toList) yield (razie.Timer {
    ctx.bind(ctx,ctx.parser)
//    ScalaScript("1+2").eval(ctx)
    })).mkString("\n")+"\n") :: Nil
    
    println(pr)
}

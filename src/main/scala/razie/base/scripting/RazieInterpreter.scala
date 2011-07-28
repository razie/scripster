/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting

import scala.tools.{ nsc => nsc }
import scala.tools.nsc.{ InterpreterResults => IR }
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.interpreter.IMain

/** hacking the scala interpreter - this will accumulate errors and retrieve all new defined values */
class RazieInterpreter(s: nsc.Settings) extends nsc.Interpreter(s) {
  import memberHandlers._

  // What Razie needs in Request
  case class PublicRequest(usedNames: List[String], valueNames: List[String], extractionValue: Option[Any], err: List[String])

  def lastRequest: Option[PublicRequest] =
    prevRequestList.lastOption map (l =>
      PublicRequest(
        l.referencedNames.map(_.decode),
        l.handlers.collect { case x: ValHandler => x.name }.map(_.decode),
        try l.getEval catch { case _ => None }, // TODO hides some errors
        errAccumulator.toList))

  // TODO this needs cleared after every run...ugly - no time to fix. should group lastErr under lastRequ
  private var errAccumulator = new scala.collection.mutable.ListBuffer[String]()

  /** hacked reporter - accumulates erorrs... */
  lazy override val reporter: ConsoleReporter = new IMain.ReplReporter(this) {
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
          try lastRequest.flatMap(_.extractionValue).get.asInstanceOf[T]
          catch { case e: Exception => out println e; throw e }
        case IR.Error => {
          val c =
            if (lastRequest.get.valueNames.contains("lastException"))
              RazScript.RSError(evalExpr[Exception]("lastException").getMessage)
            else RazScript.RSError(lastRequest.get.err mkString "\n\r")
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
      x <- lastRequest.get.valueNames if (x != "lastException" && !x.startsWith("synthvar$") && x != "ctx")
    ) {
      val xx = (x -> evalExpr[Any](x))
      razie.Debug("bound: " + xx)
      //      ret += (x -> evalExpr[Any](x))
      ret += xx
    }
    ret
  }

}

/** utilities */
object RazieInterpreter {
  /** make a new parser/interpreter instance using the given error logger */
  def mkParser(errLogger: String => Unit) = {
    // when in managed class loaders we can't just use the javacp
    // TODO make this work for any managed classloader - it's hardcoded for sbt
    val env = {
      if (ScalaScript.getClass.getClassLoader.getResource("app.class.path") != null) {
        razie.Debug("Scripster using app.class.path and boot.class.path")
        // see http://gist.github.com/404272
        val settings = new nsc.Settings(errLogger)
        settings embeddedDefaults getClass.getClassLoader
        razie.Debug("Scripster using classpath: " + settings.classpath.value)
        razie.Debug("Scripster using boot classpath: " + settings.bootclasspath.value)
        settings
      } else {
        razie.Debug("Scripster using java classpath")
        val env = new nsc.Settings(errLogger)
        env.usejavacp.value = true
        env
      }
    }

    val p = new RazieInterpreter(env)
    p.setContextClassLoader
    p
  }

}

object Main34 extends App {
  val ctx = ScalaScriptContext("a" -> 1)
  val res = ScalaScript("val c = a+-+b").eval(ctx) match {
    case RazScript.RSError(msg) => msg
    case _ => ""
  }
  println(res)
  res
}

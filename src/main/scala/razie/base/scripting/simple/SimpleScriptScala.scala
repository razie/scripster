/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting.simple

import scala.tools.{ nsc => nsc }
//import scala.tools.nsc.{ InterpreterResults => IR, Settings }
import scala.tools.nsc.interpreter.IR
import scala.tools.nsc.Settings 

/* self-contained outline of the scripster, useful when opening tickets etc - otherwise not used... */

/**
 * nice result specification for a script runner
 */
object RazScript {
  // the result of running a script
  class RSResult[+A] {
    def map[B >: A](f: A => B): RSResult[B] = RSUnsupported("by default")
    def getOrElse[B >: A](f: => B): B = f
    def getOrThrow: A = throw new IllegalArgumentException(this.toString)
    def jgetOrElse(f: Any): Any = getOrElse(f)
  }

  case class RSSucc[A](res: A) extends RSResult[A] {
    override def map[B](f: A => B): RSResult[B] = RSSucc(f(res))
    override def getOrElse[B >: A](f: => B): B = res
    override def getOrThrow: A = res
  }

  case class RSError(err: String) extends RSResult[String]
  object RSIncomplete extends RSResult[Any] // expression is incomplete...
  case class RSUnsupported(what: String) extends RSResult[Nothing] // interactive mode unsupported
  object RSSuccNoValue extends RSResult[Any] // successful, but no value returned

  def err(msg: String) = RSError(msg)
  def succ(res: AnyRef) = RSSucc(res)
}

/**
 * The context in which the scripts are evaluated.
 *
 * Will cache the environment, including the parser instance.
 * That way things defined in one script are visible to the next
 *
 * It also defines the vlaues available to the scripts. Values can be modified/added to by previous scripts
 */
case class ScriptContext(var values: Map[String, Any]) {
  val p = SS mkParser err
  var lastError: String = ""

  lazy val c = new nsc.interpreter.JLineCompletion(p)

  /** content assist options */
  def options(scr: String): java.util.List[String] = {
    SS.bind(this, p)
    val l = new java.util.ArrayList[String]()
    val output = c.completer().complete(scr, scr.length)
    import scala.collection.JavaConversions._
    l.addAll(output.candidates)
    l
  }

  def err(s: String): Unit = { lastError = s }
}

// statics
object SS {

  // bind a context and its values
  def bind(ctx: ScriptContext, p: nsc.Interpreter) {
    p.bind("ctx", ctx.getClass.getCanonicalName, ctx)

    ctx.values foreach (m => p.bind(m._1, m._2.asInstanceOf[AnyRef].getClass.getCanonicalName, m._2))
  }

  // make a custom parser
  // see http://gist.github.com/404272
  def mkParser(errLogger: String => Unit) = {
    if (SS.getClass.getClassLoader.getResource("app.class.path") != null) {
      val settings = new Settings(errLogger)
      settings embeddedDefaults getClass.getClassLoader
      println(">>>>>>>>>>>>>>" + settings.classpath.value)
      println(">>>>>>>>>>>>>>" + settings.bootclasspath.value)

      val p = new RazieInterpreterImpl(settings) {
        override protected def parentClassLoader = SS.getClass.getClassLoader
      }
      p.setContextClassLoader
      p
    } else {
      val env = new nsc.Settings(errLogger)
      env.usejavacp.value = true

      val p = new RazieInterpreterImpl(env)
      p.setContextClassLoader
      p
    }
  }
}

import java.io.File
import File.{ pathSeparator => / }
import scala.io.Source

class Holder { var value: Any = _ }

import scala.tools.nsc.{ GenericRunnerSettings, Interpreter, Settings }

/** an interpreted scala script */
case class AScript(val script: String) extends razie.Logging {

  def print = { println("scala:\n" + script); this }

  // evaluate as an independent expression
  def eval(ctx: ScriptContext): RazScript.RSResult[Any] = {
    var result: AnyRef = "";

    val p = ctx.p

    try {
      SS.bind(ctx, p)

      // this see http://lampsvn.epfl.ch/trac/scala/ticket/874 at the end, there was some work with jsr223

      // Now evaluate the script

      val r = p.evalExpr[Any](script)

      if (r != null) result = r.asInstanceOf[AnyRef]

      // bind new names back into context
      p.lastNames.foreach(m => ctx.values += (m._1 -> m._2))

      RazScript.RSSucc(result)
    } catch {
      case e: Exception => {
        log("While processing script: " + this.script, e)
        val r = "ERROR: " + e.getMessage + " : " +
          (ctx.lastError)
        ctx.lastError
        RazScript.RSError(r)
      }
    }
  }

  // evaluate as part of a series of expressions in an interactive environment
  def interactive(ctx: ScriptContext): RazScript.RSResult[Any] = {

    val p = ctx.p

    try {
      SS.bind(ctx, p)

      val ret = p.eval(this, ctx)

      // bind new names back into context
      p.lastNames.foreach(m => ctx.values += (m._1 -> m._2))

      ret
    } catch {
      case e: Exception => {
        log("While processing script: " + this.script, e)
        throw e
      }
    }
  }

}

/** hacking the scala interpreter - this will accumulate errors and retrieve all new defined values */
class RazieInterpreterImpl(s: nsc.Settings) extends nsc.Interpreter(s) {
  import memberHandlers._
  import scala.tools.nsc.reporters.ConsoleReporter
  import scala.tools.nsc.interpreter.IMain
  import scala.tools.nsc.interpreter.ReplReporter
//  import nsc.interpreter.MemberHandlers

  // What Razie needs in Request
  case class PublicRequest(usedNames: List[String], valueNames: List[String], extractionValue: Option[Any], err: List[String])

  // 3. Can't get the actual error message
  // TODO this needs cleared after every run...ugly - no time to fix. should group lastErr under lastRequ
  var errAccumulator = new scala.collection.mutable.ListBuffer[String]()

  /** hacked reporter - accumulates erorrs... */
  override lazy val reporter = new ReplReporter(this) {
    override def printMessage(msg: String) {
      errAccumulator append msg
      out println msg
      //      out println clean(msg)
      out.flush()
    }
  }

  // 1. Request is private. I need: dependencies (usedNames?) newly defined values (boundNames?)
  // the resulting value and the error message(s) if any
  // 2. Can't get the last request
  def razlastRequest: Option[PublicRequest] =
    prevRequestList.lastOption map (l =>
      //       PublicRequest (l.usedNames.map(_.decode), l.valueNames.map(_.decode), l.extractionValue, errAccumulator.toList)
      PublicRequest(
        l.referencedNames.map(_.decode),
        l.handlers.collect { case x: ValHandler => x.name }.map(_.decode),
        try l.lineRep.callOpt("$result") catch { case _ => None }, //l.extractionValue,  // TODO hides some errors
//worked in 2.9.0-1        try l.getEval catch { case _ => None }, //l.extractionValue,  // TODO hides some errors
        errAccumulator.toList))

  //  def allImplicits                   = allHandlers filter (_.definesImplicit) flatMap (_.definedNames)
  //  def importHandlers                 = allHandlers collect { case x: ImportHandler => x }

  //  /** hacked reporter - accumulates erorrs... */
  //  class MyPrintWriter extends nsc.NewLinePrintWriter(new ConsoleWriter, true) {
  //    override def println() { print("\n"); flush() }
  //  }

  def evalExpr[T](code: String): T = {
    beQuietDuring {
      interpret(code) match {
        case IR.Success =>
          val x = razlastRequest.get
          val b = x.extractionValue
          try razlastRequest.flatMap(_.extractionValue).get.asInstanceOf[T]
          catch { case e: Exception => out println e; throw e }
        case _ => throw new IllegalStateException("parser didn't return success")
      }
    }
  }

  def eval(s: AScript, ctx: ScriptContext): RazScript.RSResult[Any] = {
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

object D {
  def stop {
    println("stopped")
  }
}

/** scripting examples */
object SimpleSSSamples extends App {
  var failed = 0
  var passed = 0
  var skipped = 0

  def expect(x: Any)(f: => Any) = {
    val r = f;
    if (x == r) {
      println("OK: " + x + "...as expected")
      passed += 1
    } else {
      println("ERROR - Expected " + x + " : " + x.asInstanceOf[AnyRef].getClass + " but got " + r + " : " + r.asInstanceOf[AnyRef].getClass)
      failed = failed + 1
    }
  }

  def dontexpect(x: Any)(f: => Any) = {
    println("Skipping...")
    skipped += 1
  }

  // simple, one time, expression
  expect(3) {
    AScript("1+2").print.eval(ScriptContext(Map())) getOrElse "?"
  }

  // test binding variables
  expect("12") {
    AScript("a+b").print.eval(ScriptContext(Map("a" -> "1", "b" -> "2"))) getOrElse "?"
  }

  // test sharing variables - this is possible because populated variables end up in the context and we 
  // share the context
  expect("12") {
    val ctx = ScriptContext(Map("a" -> "1", "b" -> "2"))
    AScript("val c = a+b").print.interactive(ctx)
    AScript("c").print.interactive(ctx) getOrElse "?"
  }

  // options
  expect(true) {
    val ctx = ScriptContext(Map("a" -> 1, "b" -> 2))
    ctx.options("java.lang.Sys") contains ("System")
  }

  // export new variables back into context
  expect("12") {
    val ctx = ScriptContext(Map("a" -> "1", "b" -> "2"))
    AScript("val c = a+b").print.interactive(ctx)
    ctx.values getOrElse ("c", "?")
  }

  // test sharing defs
  expect(9) {
    val ctx = ScriptContext(Map("a" -> 1, "b" -> 2))
    D.stop
    AScript("""def f(x: Int) = x*x""").print.interactive(ctx)
    AScript("""f (1+2)""").print.interactive(ctx) getOrElse "?"
  }

  // TOOD why can't i bind integers?
  expect(3) {
    val ctx = ScriptContext(Map("a" -> 1, "b" -> 2))
    AScript("val c = a+b").print.interactive(ctx)
    ctx.values getOrElse ("c", "?")
  }

  if (failed > 0)
    println("====================FAILED " + failed + " tests=============")
  else
    println("ok, passed %s tests, skipped $s tests".format(passed, skipped))
} 

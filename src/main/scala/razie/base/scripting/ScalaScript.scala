/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting

import razie.base.ActionContext
import scala.tools.{ nsc => nsc }
import scala.tools.nsc.{ InterpreterResults => IR }

/** will cache the environment, including the parser instance. 
 * That way things defined in one script are visible to the next */
class ScalaScriptContext(parent: ActionContext = null) extends ScriptContextImpl(parent) {
//  val p = ScalaScript mkParser err
  val soon = razie.Threads.promise {
    val ppp = ScalaScript mkParser err
    ppp.evalExpr[Any]("1+2") // prime the parser
    ppp
    }
  lazy val p = soon.get() // blocking call on Future
  
  lazy val c = new nsc.interpreter.Completion(p)

  def this(parent: ActionContext, args: Any*) = {
    this(parent)
    // TODO I'm loosing the scala types. JavaAttrAccessImpl needs to become scala
    setAttr(args map (_.asInstanceOf[AnyRef]): _*)
  }

  /** content assist options */
  override def options(scr: String): java.util.List[String] = {
    ScalaScript.bind(this, p)
    val l = new java.util.ArrayList[String]()
    c.jline.complete(scr, scr.length, l)
    val itDoesntWorkOtherwise = l.toString
    l
  }

  def err(s: String): Unit = { lastError = s }

  var expr: Boolean = true // I'm in expression mode versus interpret mode
}

// statics
object ScalaScript {
  /** bind the values inside the context to the given interpreter instance */
  def bind(ctx: ActionContext, p: nsc.Interpreter) {
    ibind(ctx, p)
  }

  /** bind the values inside the context to the given interpreter instance */
  private[this] def ibind(ctx: ActionContext, p: nsc.Interpreter) {
    p.bind("ctx", ctx.getClass.getCanonicalName, ctx)
    if (ctx.isInstanceOf[ScriptContextImpl] && ctx.asInstanceOf[ScriptContextImpl].parent != null)
      p.bind("parent", ctx.asInstanceOf[ScriptContextImpl].parent.getClass.getCanonicalName, ctx.asInstanceOf[ScriptContextImpl].parent)

    val iter = ctx.getPopulatedAttr().iterator
    while (iter.hasNext) {
      val key = iter.next
      val obj = ctx.getAttr(key);
      if ("ctx" != key && "parent" != key)
        razie.Debug ("binding " + key + ":" + obj.getClass.getName) // obj.toString causes a mess...
      //        razie.Debug ("binding " + key + ":"+obj.getClass.getSimpleName) // obj.toString causes a mess...
      //      p.bind(key, obj.getClass.getCanonicalName, obj)
      p.bind(key, obj.getClass.getName, obj)
    }
  }

  /** make a new parser/interpreter instance using the given error logger */
  def mkParser(errLogger: String => Unit) = {
    // when in managed class loaders we can't just use the javacp
    // TODO make this work for any managed classloader - it's hardcoded for sbt
    val env = {
      if (ScalaScript.getClass.getClassLoader.getResource("app.class.path") != null) {
        razie.Debug ("Scripster using app.class.path and boot.class.path")
        // see http://gist.github.com/404272
        val settings = new nsc.Settings(errLogger)
        settings embeddedDefaults getClass.getClassLoader
        //razie.Debug ("Scripster using classpath: " + settings.classpath.value)
        //razie.Debug ("Scripster using boot classpath: " + settings.bootclasspath.value)
        settings
      } else {
        razie.Debug ("Scripster using java classpath")
        val env = new nsc.Settings(errLogger)
        env.usejavacp.value = true
        env
      }
    }

    val p = new RaziesInterpreter(env)
    p.setContextClassLoader
    p
  }
}

/** an interpreted scala script */
case class ScalaScript(val script: String) extends RazScript {

  /** @return the statement */
  override def toString() = "scala:\n" + script

  /**
   * execute the script with the given context
   * 
   * @param c the context for the script
   */
  override def eval(ctx: ActionContext): RazScript.RSResult[Any] = {
    var result: AnyRef = "";

    val sctx: Option[ScalaScriptContext] =
      if (ctx.isInstanceOf[ScalaScriptContext])
        Some(ctx.asInstanceOf[ScalaScriptContext])
      else None

    val p = sctx.map(_.p) getOrElse (ScalaScript mkParser println)

    try {
      ScalaScript.bind(ctx, p)

      // this see http://lampsvn.epfl.ch/trac/scala/ticket/874 at the end, there was some work with jsr223

      // Now evaluate the script

      val r = p.evalExpr[Any](script)

      // TODO why was I converting to String?
      // convert to String
      //      result = if (r==null) "" else r.toString
      if (r != null) result = r.asInstanceOf[AnyRef]

      // bind new names back into context
      p.lastNames.foreach(m => ctx.set(m._1, m._2.asInstanceOf[AnyRef]))

      RazScript.RSSucc(result)
    } catch {
      case e: Exception => {
          razie.Log("While processing script: " + this.script, e)
          val r = "ERROR: " + e.getMessage + " : " +
            (sctx.map(_.lastError) getOrElse "Unknown")
          sctx.map(_.lastError = "")
          RazScript.RSError(r)
        }
    }
  }

  /**
   * execute the script with the given context
   * 
   * @param c the context for the script
   */
  def interactive(ctx: ActionContext): RazScript.RSResult[Any] = {
    val sctx: Option[ScalaScriptContext] =
      if (ctx.isInstanceOf[ScalaScriptContext])
        Some(ctx.asInstanceOf[ScalaScriptContext])
      else None

    val p = sctx.map(_.p) getOrElse (ScalaScript mkParser println)

    try {
      ScalaScript.bind(ctx, p)

      val ret = p.eval(this, ctx)

      // bind new names back into context
      p.lastNames.foreach(m => ctx.set(m._1, m._2.asInstanceOf[AnyRef]))

      ret
    } catch {
      case e: Exception => {
          razie.Log("While processing script: " + this.script, e)
          throw e
        }
    }
  }

  def compile(ctx: ActionContext): RazScript.RSResult[Any] = RazScript.RSUnsupported("ScriptScala.compile() TODO ")
}

/** hacking the scala interpreter - this will accumulate errors and retrieve all new defined values */
class RaziesInterpreter(s: nsc.Settings) extends nsc.Interpreter(s) {

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

/** scripting examples in ScriptScalaTest.scala */

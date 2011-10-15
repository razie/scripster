/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting

import razie.base.ActionContext
import scala.tools.{ nsc => nsc }
import scala.tools.nsc.{ InterpreterResults => IR }

/**
 * will cache the environment, including the parser instance.
 * That way things defined in one script are visible to the next
 */
class ScalaScriptContext(parent: ActionContext = null) extends ScriptContextImpl(parent) {

  private[this] val soon = razie.Threads.promise {
    val ppp = ScalaScript mkParser err
    ppp.evalExpr[Any]("1+2") // prime the parser
    ppp
  }
  lazy val parser = soon.get() // blocking call on Future

  lazy val comp = new nsc.interpreter.JLineCompletion(parser)

  def this(parent: ActionContext, args: Any*) = {
    this(parent)
    // TODO I'm loosing the scala types. JavaAttrAccessImpl needs to become scala
    setAttr(args map (_.asInstanceOf[AnyRef]): _*)
  }

  /** content assist options */
  override def options(scr: String, pos:Int): java.util.List[String] = {
    ScalaScript.bind(this, parser)
    val l = new java.util.ArrayList[String]()
    val newscr1 = scr.replaceFirst("^[ \t]+", "")
    val newscr2 = newscr1.trim
    val newpos = pos-(scr.length-newscr1.length)
    val output = comp.completer().complete(newscr2, newpos)
    import scala.collection.JavaConversions._
    l.addAll(output.candidates)
    l
  }

  var lastError: String = null

  def err(s: String): Unit = { lastError = s }

  var expr: Boolean = true // I'm in expression mode versus interpret mode
}

/** utility builders */
object ScalaScriptContext {
  def apply(parms: Map[String, Any]) = {
    val s = new ScalaScriptContext(null)
    parms foreach (t => s.set(t._1, t._2))
    s
  }
  def apply(parms: (String, Any)*) = {
    val s = new ScalaScriptContext(null)
    parms foreach (t => s.set(t._1, t._2))
    s
  }
}

/** some statics */
object ScalaScript {
  def apply(s: String) = new ScalaScript(s)

  /**
   * bind the values inside the context to the given interpreter instance
   *
   * NOTE that binding errors are ignored - there's just too many...watch your log for errors
   */
  def bind(ctx: ActionContext, p: nsc.Interpreter) {
    p.bind("ctx", ctx.getClass.getCanonicalName, ctx)
    if (ctx.isInstanceOf[ScriptContextImpl] && ctx.asInstanceOf[ScriptContextImpl].parent != null)
      p.bind("parent", ctx.asInstanceOf[ScriptContextImpl].parent.getClass.getCanonicalName, ctx.asInstanceOf[ScriptContextImpl].parent)

    ctx.foreach { (name, value) =>
      if ("ctx" != name && "parent" != name) {
        razie.Debug("binding " + name + ":" + value.getClass.getName) // obj.toString causes a mess...

        // this here reveals a screwed up handling of $$ class names in scala
        //        razie.Debug ("binding " + name + ":"+value.getClass.getSimpleName) // obj.toString causes a mess...
        //      p.bind(name, value.getClass.getCanonicalName, value)

        try {
          p.bind(name, value.getClass.getName, value)
        } catch {
          case e: Exception => {
            razie.Alarm("While binding variable: " + name + ":" + value.getClass.getName, e)
          }
        }
      }
    }
  }

  /** make a new parser/interpreter instance using the given error logger */
  def mkParser(errLogger: String => Unit) = RazieInterpreter mkParser errLogger
  
  /** convenience - make a new context here */
  def mkContext = new ScalaScriptContext()
}

/** an interpreted scala script */
class ScalaScript(val script: String) extends RazScript with razie.Logging {

  /** @return the statement */
  override def toString() = "scala:\n" + script

  /**
   * execute the script with the given context
   *
   * @param c the context for the script
   */
  override def eval(ctx: ActionContext): RazScript.RSResult[Any] = {
    var result: AnyRef = "";

    // specific scala contexts can cache a parser
    val sctx: Option[ScalaScriptContext] =
      if (ctx.isInstanceOf[ScalaScriptContext])
        Some(ctx.asInstanceOf[ScalaScriptContext])
      else None

    val p = sctx.map(_.parser) getOrElse (ScalaScript mkParser println)

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
        razie.Warn("While processing script: " + this.script, e)
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

    val p = sctx.map(_.parser) getOrElse (ScalaScript mkParser println)

    try {
      ScalaScript.bind(ctx, p)

      val ret = p.eval(this)

      // bind new names back into context
      p.lastNames.foreach(m => ctx.set(m._1, m._2.asInstanceOf[AnyRef]))

      ret
    } catch {
      case e: Exception => {
        log("While processing script: " + this.script, e)
        throw e
      }
    }
  }

  def compile(ctx: ActionContext): RazScript.RSResult[Any] = RazScript.RSUnsupported("ScriptScala.compile() TODO ")

  override def lang = "scala"
}

/** scripting examples in ScriptScalaTest.scala */

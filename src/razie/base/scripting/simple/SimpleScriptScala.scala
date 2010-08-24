/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting.simple

import scala.tools.{nsc => nsc}
import scala.tools.nsc.{ InterpreterResults => IR }

/**
 * nice result specification for a script runner
 */
object RazScript {
   // the result of running a script
  class RSResult[+A] { 
    def map[B>:A] (f:A=>B) : RSResult[B] = RSUnsupported ("by default")
    def getOrElse[B >: A] (f: => B) : B = f
    def getOrThrow:A = throw new IllegalArgumentException (this.toString)
    def jgetOrElse (f:Any) : Any = getOrElse(f)
  }

  case class RSSucc[A] (res:A) extends RSResult[A] { 
    override def map[B] (f:A=>B) : RSResult[B] = RSSucc(f(res))
    override def getOrElse[B >: A] (f: => B) : B = res
    override def getOrThrow:A = res
  }

  case class RSError (err:String) extends RSResult[String]
      object RSIncomplete  extends RSResult[Any]   // expression is incomplete...
  case class RSUnsupported (what:String) extends RSResult[Nothing] // interactive mode unsupported
      object RSSuccNoValue extends RSResult[Any] // successful, but no value returned
  
  def err (msg:String) = RSError(msg)
  def succ (res:AnyRef) = RSSucc(res)
}

/** 
 * The context in which the scripts are evaluated.
 * 
 * Will cache the environment, including the parser instance. 
 * That way things defined in one script are visible to the next 
 * 
 * It also defines the vlaues available to the scripts. Values can be modified/added to by previous scripts
 */
case class ScriptContext (var values:Map[String, Any]) {
   val p = SS mkParser err
   var lastError : String = ""
   
   lazy val c = new nsc.interpreter.Completion(p)
   
   /** content assist options */
   def options (scr:String) : java.util.List[String] = {
      SS.bind (this, p)
      val l = new java.util.ArrayList[String]()
      c.jline.complete (scr, scr.length, l)
      val itDoesntWorkOtherwise = l.toString
      l
   }
   
   def err (s:String) : Unit = { lastError = s }
}

// statics
object SS {

  // bind a context and its values
  def bind (ctx:ScriptContext, p:nsc.Interpreter) {
    p.bind ("ctx", ctx.getClass.getCanonicalName, ctx)

    ctx.values foreach (m => p.bind (m._1, m._2.asInstanceOf[AnyRef].getClass.getCanonicalName, m._2))
  }

  // make a custom parser
  def mkParser (errLogger: String => Unit) = {
    val env = {
       val set = new nsc.Settings(errLogger)
  //     set.classpath.value += java.io.File.pathSeparator + System.getProperty ("java.class.path")
       set.usejavacp.value = true
       set
    }
  
    val p = new RaziesInterpreter (env) 
  
    p.setContextClassLoader  
    p
  }
}

/** an interpreted scala script */
case class AScript (val script:String) {

   def print = { println ("scala:\n" + script); this}

   // evaluate as an independent expression
   def eval (ctx:ScriptContext) : RazScript.RSResult[Any] = {
      var result:AnyRef = "";

      val p = ctx.p
      
      try {
         SS.bind(ctx, p)

         // this see http://lampsvn.epfl.ch/trac/scala/ticket/874 at the end, there was some work with jsr223
            
         // Now evaluate the script

         val r =  p.evalExpr[Any] (script)

         if (r!=null) result = r.asInstanceOf[AnyRef]

         // bind new names back into context
         p.lastNames.foreach (m => ctx.values += (m._1 -> m._2))
           
         RazScript.RSSucc(result)
        } catch {
          case e:Exception => {
            razie.Log ("While processing script: " + this.script, e)
            val r = "ERROR: " + e.getMessage + " : " + 
                     (ctx.lastError)
            ctx.lastError
            RazScript.RSError(r)
          }
        }
    }

   // evaluate as part of a series of expressions in an interactive environment
   def interactive(ctx:ScriptContext) : RazScript.RSResult[Any] = {

      val p = ctx.p
      
      try {
         SS.bind(ctx, p)

         val ret = p.eval(this, ctx)
         
         // bind new names back into context
         p.lastNames.foreach (m => ctx.values += (m._1 -> m._2))
         
         ret
        } catch {
          case e:Exception => {
            razie.Log ("While processing script: " + this.script, e)
            throw e
          }
        }
    }
   
}

/** hacking the scala interpreter */
class RaziesInterpreter (s:nsc.Settings) extends nsc.Interpreter (s) {

  /** transform interpreter codes into reacher Raz codes */
  def eval (s:AScript, ctx:ScriptContext) : RazScript.RSResult[Any] = {
    beQuietDuring {
      interpret(s.script) match {
        case IR.Success => 
          if (lastRequest map (_.extractionValue.isDefined) getOrElse false) 
             RazScript.RSSucc (lastRequest.get.extractionValue get)
          else
             RazScript.RSSuccNoValue
        case IR.Error => {
           val c = RazScript.RSError (lastRequest.get.err mkString "\n\r")
           errAccumulator.clear
           c
        }
        case IR.Incomplete => RazScript.RSIncomplete
     }
    }
  }

  def lastNames = {
    // TODO nicer way to build a map from a list?
    val ret = new scala.collection.mutable.HashMap[String,Any]()
    // TODO get the value of x nicer
    lastRequest.get.valueNames.foreach (x => {
       val xx = (x -> evalExpr[Any] (x))
       println ("bound: " + xx)
       ret += (x -> evalExpr[Any] (x))
    })
    ret
  }
}

/** scripting examples */
object SimpleSSSamples extends Application {
  var failed = 0
  
  def expect (x:Any) (f: => Any) = { 
     val r = f; 
     if (x == r) 
        println (x + "...as expected")
     else {
        println ("Expected "+x+" : "+x.asInstanceOf[AnyRef].getClass+" but got "+r+" : "+r.asInstanceOf[AnyRef].getClass) 
        failed=failed+1
     }
     }
  
  def dontexpect (x:Any) (f: => Any) = { 
     println ("Skipping...")
  }
  
  // simple, one time, expression
  expect (3) { 
    AScript("1+2").print.eval (ScriptContext(Map())) getOrElse "?"
    }

  // test binding variables
  expect ("12") {
    AScript ("a+b").print.eval (ScriptContext(Map("a" -> "1", "b" -> "2"))) getOrElse "?"
    }

  // test sharing variables - this is possible because populated variables end up in the context and we 
  // share the context
  expect ("12") {
     val ctx = ScriptContext(Map("a" -> "1", "b" -> "2"))
     AScript("val c = a+b").print.interactive (ctx)
     AScript("c").print.interactive (ctx) getOrElse "?"
     }

  // options
  expect (true) {
     val ctx = ScriptContext(Map("a" -> 1, "b" -> 2))
     ctx.options ("java.lang.Sys") contains ("System")
     }

  // export new variables back into context
  expect ("12") {
     val ctx = ScriptContext(Map("a" -> "1", "b" -> "2")) 
     AScript("val c = a+b").print.interactive (ctx)
     ctx.values getOrElse ("c", "?")
     }

  // test sharing defs
  expect (9) {
     val ctx = ScriptContext(Map("a" -> 1, "b" -> 2))
     AScript("""def f(x: Int) = x*x""").print.interactive (ctx)
     AScript("""f (1+2)""").print.interactive (ctx) getOrElse "?"
     }

  // TOOD why can't i bind integers?
  expect (3) {
     val ctx = ScriptContext(Map("a" -> 1, "b" -> 2))
     AScript("val c = a+b").print.interactive (ctx)
     ctx.values getOrElse ("c", "?")
     }


  if (failed > 0)
     println ("====================FAILED "+failed+" tests=============")
  else
     println ("ok") 
} 

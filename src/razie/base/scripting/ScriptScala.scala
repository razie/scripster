/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting

import razie.base.ActionContext
import scala.tools.{nsc => nsc}
import scala.tools.nsc.{ InterpreterResults => IR }

/** will cache the environment */
class ScalaScriptContext (parent:ActionContext = null) extends ScriptContextImpl (parent) {
   val env = new nsc.Settings (err)
   val p = new RaziesInterpreter (env)         
   
   def this (parent:ActionContext, args:Any*)  = {
      this (parent)
      setAttr (args)
   }
   
   lazy val c = {
      // not just create, but prime this ... first time it doesn't work...
      SS.bind (this, p)
      val cc = new nsc.interpreter.Completion(p)
      var scr = "java.lang.Sys"
      val l = new java.util.ArrayList[String]()
      cc.jline.complete (scr, scr.length-1, l)
      cc
   }
   
      /** content assist options */
   override def options (scr:String) : java.util.List[String] = {
      SS.bind (this, p)
      val l = new java.util.ArrayList[String]()
      c.jline.complete (scr, scr.length, l)
      val itDoesntWorkOtherwise = l.toString
      l
   }
   
   def err (s:String) : Unit = { lastError = s }
   
   var expr : Boolean = true // I'm in expression mode versus interpret mode
}

// statics
object SS {
  def bind (ctx:ActionContext, p:nsc.Interpreter) {
    p.bind ("ctx", ctx.getClass.getCanonicalName, ctx)
    if (ctx.isInstanceOf[ScriptContextImpl] && ctx.asInstanceOf[ScriptContextImpl].parent != null)
       p.bind ("parent", ctx.asInstanceOf[ScriptContextImpl].parent.getClass.getCanonicalName, ctx.asInstanceOf[ScriptContextImpl].parent)

    val iter = ctx.getPopulatedAttr().iterator
    while (iter.hasNext) {
      val key = iter.next
      val obj = ctx.getAttr(key);
      p.bind (key, obj.getClass.getCanonicalName, obj)
    }
  }
}

/** an interpreted scala script */
class ScriptScala (val script:String) extends RazScript {

    /** @return the statement */
    override def toString() = "scala:\n" + script

    /**
     * execute the script with the given context
     * 
     * @param c the context for the script
     */
   override def eval(ctx:ActionContext) : RazScript.RSResult[Any] = {
      var result:AnyRef = "";

      val sctx : Option[ScalaScriptContext] = 
       if (ctx.isInstanceOf[ScalaScriptContext])
         Some(ctx.asInstanceOf[ScalaScriptContext])
       else None

      val env = sctx.map (_.env) getOrElse new scala.tools.nsc.Settings
      val p = sctx.map (_.p) getOrElse new scala.tools.nsc.Interpreter (env)         
      
      try {
         SS.bind(ctx, p)

         // this see http://lampsvn.epfl.ch/trac/scala/ticket/874 at the end, there was some work with jsr223
            
            // Now evaluate the script

            val r =  p.evalExpr[Any] (script)

            // convert to String
            result = if (r==null) "" else r.toString

            // TODO put back all variables
           RazScript.RSSucc(result)
        } catch {
          case e:Exception => {
            razie.Log ("While processing script: " + this.script, e)
            val r = "ERROR: " + e.getMessage + " : " + 
                     (sctx.map (_.lastError) getOrElse "Unknown")
            sctx.map (_.lastError = "")
            RazScript.RSError(r)
          }
        }
    }
   
    /**
     * execute the script with the given context
     * 
     * @param c the context for the script
     */
   def interactive(ctx:ActionContext) : RazScript.RSResult[Any] = {
      val sctx : Option[ScalaScriptContext] = 
       if (ctx.isInstanceOf[ScalaScriptContext])
         Some(ctx.asInstanceOf[ScalaScriptContext])
       else None

      val env = sctx.map (_.env) getOrElse new scala.tools.nsc.Settings
      val p = sctx.map (_.p) getOrElse new RaziesInterpreter (env)         
      
      try {
         SS.bind(ctx, p)

         p.eval(this, ctx)
         
        // TODO put back all variables
        } catch {
          case e:Exception => {
            razie.Log ("While processing script: " + this.script, e)
            throw e
          }
        }
    }
   
   def compile(ctx:ActionContext) : RazScript.RSResult[Any] = RazScript.RSUnsupported
}

/** hacking the scala interpreter */
class RaziesInterpreter (s:nsc.Settings) extends nsc.Interpreter (s) {
  
  def eval (s:ScriptScala, ctx:ActionContext) : RazScript.RSResult[Any] = {
    beQuietDuring {
      interpret(s.script) match {
        case IR.Success => 
          if (razLastReq.extractionValue.isDefined) 
             RazScript.RSSucc (razLastReq.extractionValue get)
          else
             RazScript.RSSuccNoValue
        case IR.Error => {
           val c = RazScript.RSError (razLastReq.err mkString "\n\r")
           razAccerr.clear
           c
        }
        case IR.Incomplete => RazScript.RSIncomplete
     }
    }
  }
}

/** a test app */
object ScriptScalaTestApp extends Application{
    var script = "val y = 3; def f(x:Int)={x+1}; val res=f(7); res";
    var js = new ScriptScala(script);
    System.out.println(js.eval(ScriptFactory.mkContext()));

    script = "TimeOfDay.value()";
    js = new ScriptScala(script);
    var ctx = ScriptFactory.mkContext();
    ctx.setAttr("TimeOfDay", new TimeOfDay(), null);
    System.out.println(js.eval(ctx));
    
    js.eval(ctx).map(println (_))
}

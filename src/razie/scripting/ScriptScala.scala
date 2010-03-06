/**  ____    __    ____  ____  ____/___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___) __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__)\__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)___/   (__)  (______)(____/   LICENESE.txt
 */
package razie.scripting

import com.razie.pub.base.RazScript
import razie.base.ScriptContext
import scala.tools.{nsc => nsc}

/** will cache the environment */
class ScalaScriptContext (parent:ScriptContext) extends ScriptContext.Impl (parent) {
   var lastError : String = null
   val env = new nsc.Settings (err)
   val p = new nsc.Interpreter (env)         
   
      /** content assist options */
   override def options (scr:String) : java.util.List[String] = {
      val l = new java.util.ArrayList[String]()
      val c = new nsc.interpreter.Completion(p)
      
      c.jline.complete (scr, scr.length-1, l)
      c.jline.complete (scr, scr.length-1, l)
      l
   }
   
   def err (s:String) : Unit = { lastError = s }
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
   override def eval(ctx:ScriptContext) : AnyRef = {
      var result:AnyRef = "";

      val env = if (ctx.isInstanceOf[ScalaScriptContext])
         ctx.asInstanceOf[ScalaScriptContext].env
         else new scala.tools.nsc.Settings
      val p = if (ctx.isInstanceOf[ScalaScriptContext])
         ctx.asInstanceOf[ScalaScriptContext].p
         else new scala.tools.nsc.Interpreter (env)         
      
      try {
         p.bind ("ctx", classOf[ScriptContext].getCanonicalName, ctx)
         
         val iter = ctx.getPopulatedAttr().iterator
         while (iter.hasNext) {
            val key = iter.next
            val obj = ctx.getAttr(key);
            p.bind (key, obj.getClass.getCanonicalName, obj)
         }

         // TODO fix this see http://lampsvn.epfl.ch/trac/scala/ticket/874 at the end,
         // there was some work with jsr223
            
            // Now evaluate the script
//            val r = p.interpret (script)
            val r = p.evalExpr[Any] (script)
            
            // convert to String
            result = if (r==null) "" else r.toString

            // TODO put back all variables
        } catch {
          case e:Exception => {
            razie.Log ("While processing script: " + this.script, e)
            result = "ERROR: " + e.getMessage + " : " + ctx.asInstanceOf[ScalaScriptContext].lastError
            ctx.asInstanceOf[ScalaScriptContext].lastError = ""
          }
        }
    
        result;
    }
}

/** a test app */
object ScriptScalaTestApp extends Application{
    var script = "val y = 3; def f(x:int)={x+1}; val res=f(7); res";
    var js = new ScriptScala(script);
    System.out.println(js.eval(new ScriptContext.Impl()));

    script = "TimeOfDay.value()";
    js = new ScriptScala(script);
    var ctx = new ScriptContext.Impl();
    ctx.setAttr("TimeOfDay", new TimeOfDay(), null);
    System.out.println(js.eval(ctx));
}

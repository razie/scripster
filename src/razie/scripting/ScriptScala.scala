/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.scripting

import com.razie.pub.base.RazScript
import razie.base.ScriptContext

/** will cache the environment */
class ScalaScriptContext (parent:ScriptContext) extends ScriptContext.Impl (parent) {
   val env = new scala.tools.nsc.Settings
   val p = new scala.tools.nsc.Interpreter (env)         
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
   override def eval(c:ScriptContext) : AnyRef = {
      var result:AnyRef = "";

      val env = if (c.isInstanceOf[ScalaScriptContext])
         c.asInstanceOf[ScalaScriptContext].env
         else new scala.tools.nsc.Settings
      val p = if (c.isInstanceOf[ScalaScriptContext])
         c.asInstanceOf[ScalaScriptContext].p
         else new scala.tools.nsc.Interpreter (env)         
      
      try {
         p.bind ("ctx", classOf[ScriptContext].getCanonicalName, c)
         
         val iter = c.getPopulatedAttr().iterator
         while (iter.hasNext) {
            val key = iter.next
            val obj = c.getAttr(key);
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
          case e:Exception =>
            throw new RuntimeException("While processing script: " + this.script, e);
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

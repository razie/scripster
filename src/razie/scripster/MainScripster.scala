package razie.scripster

import com.razie.pub.lightsoa._
import com.razie.pub.comms._
import com.razie.pub.base._
import com.razie.pub.base.data._
import com.razie.pub.http._
import com.razie.pub.http.sample._
import com.razie.pub.http.LightContentServer
import com.razie.pub.base.ExecutionContext
import razie.base._
import razie.scripting._

object MainScripster extends Application {
   // warm up the interpreter while you move your hands... :)
   new java.lang.Thread ( 
         new java.lang.Runnable { 
            def run() { 
               new ScriptScala ("1+2").eval(ScriptContext.Impl.global) 
               }}
         ).start
   
   Scripster.create(4445, None)
}

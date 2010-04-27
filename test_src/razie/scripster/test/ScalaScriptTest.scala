package razie.scripster.test

import org.scalatest.junit._
import razie.base.scripting._

class ScalaScriptTest extends JUnit3Suite {

  def testwpar = expect (3) { new ScriptScala ("1+2").eval (ScriptContextImpl.global)}

}

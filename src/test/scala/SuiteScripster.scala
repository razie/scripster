/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details.
 */

import org.scalatest.junit._
import org.scalatest.SuperSuite

/** main test suite */
class SuitescripsterIWishItWorked extends SuperSuite (
  List (
    new razie.scripster.test.ScalaScriptTest
  )
)

/** TODO this is sooooooooooooo messed up... */
class SuiteScripster () extends junit.framework.TestSuite(classOf[XNadaScripster]) {
  
  // this is where you list the tests...
   addTestSuite(classOf[razie.scripster.test.ScalaScriptTest])
   addTestSuite(classOf[razie.scripster.test.SimpleScriptTest])
   addTestSuite(classOf[razie.scripster.test.ScalaComplexTest])
   
   def test1() = 
     // don't touch this line
     addTest(new junit.framework.TestSuite(classOf[razie.scripster.test.ScalaScriptTest]))
     
}

// this is here to convince eclipse to run as/junit...
class XNadaScripster extends junit.framework.TestCase {
 def testNada : Unit =  {}
}


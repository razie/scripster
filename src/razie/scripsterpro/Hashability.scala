/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.scripsterpro;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.razie.pub.util.Base64;

// from http://www.coderanch.com/t/327255/Java-General/java/generate-Unique-key
object Hashability {
  
   /** hash a string */
   def hash (input:String ) : String = {
      md() update input.getBytes()
      val arr = md().digest
      Base64.encodeBytes (arr)
   }

   // I hope this initializes later, when needed...heh
   object md {
     val i = MessageDigest.getInstance("SHA")
     def apply () = { i } 
   }

   /*
    * Will produce output:
    * 
    * hash1==hash2 -->>true hash1==hash3 -->>false hash2==hash3 -->>false
    */
   def main(args:Array[String]) {
      println (hash("Joe@anonymous.identicus"));
      println (hash("Joe@anonymous.identicus"+String.valueOf(System.currentTimeMillis())));
      println (hash("Joe@anonymous.identicus"));
      println (hash("Joe@anonymous.identicus"+String.valueOf(System.currentTimeMillis())));
      println (hash("Joe@anonymous.identicus"));
      println (hash("Joe@anonymous.identicus"+String.valueOf(System.currentTimeMillis())));
      println (hash("Joe@anonymous.identicus"));
      println (hash("Joe@anonymous.identicus"+String.valueOf(System.currentTimeMillis())));
      println (hash("Joe@anonymous.identicus"));
      println (hash("Joe@anonymous.identicus"+String.valueOf(System.currentTimeMillis())));
      println (hash("Joe@anonymous.identicus"));
      println (hash("Joe@anonymous.identicus"+String.valueOf(System.currentTimeMillis())));
      println (hash("Joe@anonymous.identicus"));
      println (hash("Joe@anonymous.identicus"+String.valueOf(System.currentTimeMillis())));
      println (hash("Joe@anonymous.identicus"));
      println (hash("Joe@anonymous.identicus"+String.valueOf(System.currentTimeMillis())));
   }
}

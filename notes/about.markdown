
[Razie's Scripster](http://github.com/razie/scripster) is an interactive scripting pad for scala. It is available over telnet, http and swing. Read the guide at [http://wiki.homecloud.ca/scripster](http://wiki.homecloud.ca/scripster). Try it at http://tryscala.org

Why? So anyone can interact with a running application, having access to all or some of the application's objects. 

What is so special about the scrispter? It has full syntax coloring and content assist, for starters! 
It can be embedded into any application and shares the same port for telnet as well as web access...


Examples:
---------

Put some objects in a context and run a simple scala script:

    val context = ScalaScriptContext("a" -> "1", "b" -> "2")
    ScalaScript ("a+b").eval (context) getOrElse "?"

Create and add variables to the context, to pass to the next script:

     val ctx = ScalaScriptContext("a" -> 1, "b" -> 2)
     ScalaScript ("val c = a+b").interactive (ctx) 
     ctx getOrElse ("c", "?") // "c" is exported back into the context and available below
     ScalaScript ("c").interactive (ctx) getOrElse "?"


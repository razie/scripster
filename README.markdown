    /**  ____    __    ____  ____  ____/___      ____  __  __  ____
     *  (  _ \  /__\  (_   )(_  _)( ___) __)    (  _ \(  )(  )(  _ \
     *   )   / /(__)\  / /_  _)(_  )__)\__ \     )___/ )(__)(  ) _ <
     *  (_)\_)(__)(__)(____)(____)(____)___/    (__)  (______)(____/
     *                      
     *  Copyright (c) Razvan Cojocaru, 2007+, Creative Commons Attribution 3.0
     */

What?
=====

Embeddable interactive scripting pad for scala. It is available over telnet, http and swing. Read the guide at [http://wiki.coolscala.com/scripster](http://wiki.coolscala.com/scripster). Try it online at [http://www.tryscala.com](tryscala.com) or see [building instructions](blob/master/Building.markdown).

Why?
----
So anyone can interact with a running application, having access to all or some of the application's objects. 

What is so special about the scrispter? It has full syntax coloring and content assist, for starters! 
It can be embedded into any application and shares the same port for telnet as well as web access...

How?
----

In the sbt Project file for your project (project/build/Project.scala), add the dependency: 

    val scrip = "com.razie" % "scripster_2.9.1" % "0.8-SNAPSHOT"

If you want to build it, see [building instructions](blob/master/Building.markdown).

If instead you just want to run it or add it to your runnables as a single Jar file, use the latest distribution jar from the github downloads and use it in the classpath.


Roadmap
-------
May add some more scripting languages in the future but otherwise I don't think this will evolve.

Features for when I (or you can) find the time:
- store and access local scripts
- sign the scripts and encrypt them for safe emailing
- more secure access
- use servlet API for embedding in normal servlet-based apps
- domain local and domain signed script quoting

Examples
========

Many examples are in the junits: test_src/razie/scripster/test/

Put some objects in a context and run a simple scala script:

    val context = new ScalaScriptContext(null, "a", "1", "b", "2")
    ScriptScala ("a+b").eval (context) getOrElse "?"

Create and add variables to the context, to pass to the next script:

     val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
     ScriptScala ("val c = a+b").interactive (ctx) 
     ScriptScala ("c").interactive (ctx) getOrElse "?"

Create a scripster server: razie.scripster.MainScripster.scala

    Scripster.createServer(4445)


Baddies
=======

There's only basic policy-file based security - need to add some, especially per-script permissions. The simplest way I see to do that is to create a "client" package for all script code and grant it only specific permissions...

There's no mapping to a "normal" servlet. Jetty support etc. There is however a ScripsterService class that could serve as the basis for that. I might soon entertain moving this to play.


Architectural notes
===================

Scripster and DCI
-----------------
The simplest application will define its domain model, a few contexts and just give users access to those objects in the respective contexts. That's Scripster for you!

Users are guided through what they can do with the domain objects by the available content assist...

You may want to create UIs to nice-ify the user interaction, but, once you have your domains and contexts, with Scripster, your basic app is up and running in less than 5 minutes.


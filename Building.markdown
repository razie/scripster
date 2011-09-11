    /**  ____    __    ____  ____  ____/___      ____  __  __  ____
     *  (  _ \  /__\  (_   )(_  _)( ___) __)    (  _ \(  )(  )(  _ \
     *   )   / /(__)\  / /_  _)(_  )__)\__ \     )___/ )(__)(  ) _ <
     *  (_)\_)(__)(__)(____)(____)(____)___/    (__)  (______)(____/
     *                      
     *  Copyright (c) Razvan Cojocaru, 2007+, Creative Commons Attribution 3.0
     */

Developing & Building
---------------------

These projects are setup as eclipse projects and also have ant build and sbt build files. 
The sbt and eclipse are actively maintained...


Here's how to use it:

1. Setup sbt and scala

2. In the sbt Project file for your project (project/build/Project.scala), add the dependency:
 
    val scrip   = "com.razie" %% "scripster"       % "0.8-SNAPSHOT"


Using it in an eclipse project is easy as well.

1. Create the sbt setup for your project, as mentioned above

2. Issue the 'sbt update' command - sbt will go and download all required jars

3. In the eclipse project, add all the jar files from the lib_managed folder


Here's how to build it, if you feel like forking it:

1. Setup sbt 0.7.7 and scala 2.9.1 http://code.google.com/p/simple-build-tool/wiki/Setup

2. Make a workspace directory ${w}

3. checkout the following projects

    cd ${w}
    git clone git@github.com:razie/scripster.git

Note: if you don't have a github ssh key setup, use the anonymous checkout:

    git clone http://github.com/razie/scripster.git

4. sbt build

    cd ${w}/scripster
    sbt update publish-local


Create the distribution, with ant. Note that this distribution includes all other code except scala libraries.

1. edit ${w}/razbase/razie.properties and set the w property to the workspace

2. Build razbase, scripster and gremlins with sbt

3. Create the dist

   cd ${w}/scripster
   ant dist

8. Eclipse setup

   * install the scala 2.9.1 plugin 
   
   Do an sbt build: It's important to note that the eclipse projects depend on the sbt jar files! 

8.1. create the projects
   Create a project for: razbase, scripster

Good luck!


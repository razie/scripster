/**  ____    __    ____  ____  ____/___      ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___) __)    (  _ \(  )(  )(  _ \
 *   )   / /(__)\  / /_  _)(_  )__)\__ \     )___/ )(__)(  ) _ <
 *  (_)\_)(__)(__)(____)(____)(____)___/    (__)  (______)(____/
 *                      
 *  Copyright (c) Razvan Cojocaru, 2007+, Creative Commons Attribution 3.0
 */

What's this?
------------
Interactive scripting pad for scala. It is available over telnet, http and swing.

Read the guide at http://wiki.homecloud.ca/scripster

Why?
----
So anyone can interact with a running application, having access to all the application's objects. Obviously, we don't want 'anyone' to have this kind of access, so you will no doubt take steps to protect this.

Details
-------
The code is generally self-documented. Keep your eyes out for package.html and similar stuff.


Roadmap
-------
May add some more scripting languages in the future but otherwise I don't think this will evolve.

It may also get nicer.

Developing & Building
---------------------

These projects are setup as eclipse projects and also have ant build.xml files.

Here's how to build it:

1. Setup ant and scala
2. Make a workspace directory ${w}
3. checkout the following projects

   cd ${w}
   git clone git@github.com:razie/razbase.git
   git clone git@github.com:razie/razxml.git
   git clone git@github.com:razie/20widgets.git
   git clone git@github.com:razie/20widgets-swing.git
   git clone git@github.com:razie/razweb.git
   git clone git@github.com:razie/scripster.git

4. edit ${w}/razbase/razie.properties and set the w property to the workspace

5. build all and create the dist jar file:

   cd ${w}/scripster
   ant clean-all build-all dist

6. If you're having problems with out of memory, you have to build each:

   for ff in "razbase 20widgets 20widgets-swing razweb scripster"
   do
      ant -f $f/build.xml clean build jar-only
   done
   ant -f scripster/build.xml dist

Good luck!


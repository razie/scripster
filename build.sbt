
name := "scripster"

retrieveManaged := true // copy libs in lib_managed

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

pomExtra := (
  <url>http://www.razie.com</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://www.opensource.org/licenses/bsd-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:razie/scripster.git</url>
    <connection>scm:git:git@github.com:razie/scripster.git</connection>
  </scm>
  <developers>
    <developer>
      <id>razie</id>
      <name>Razvan Cojocaru</name>
      <url>http://www.razie.com</url>
    </developer>
  </developers>
)

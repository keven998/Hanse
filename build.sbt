name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"
val morphiaVersion = "1.0.0"
val finagleVersion = "6.30.0"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "com.lvxingpai" %% "appconfig" % "0.2.1-SNAPSHOT",
  "com.lvxingpai" %% "core-model" % "0.1.2-SNAPSHOT",
  "org.mongodb.morphia" % "morphia" % morphiaVersion,
  "org.mongodb.morphia" % "morphia-validation" % morphiaVersion,
  //thrift
  "org.apache.thrift" % "libthrift" % "0.9.3",
  "com.twitter" %% "scrooge-core" % "4.2.0",
  "com.twitter" %% "finagle-thrift" % finagleVersion,
  "com.twitter" %% "finagle-core" % finagleVersion,
  "com.twitter" %% "finagle-thrift" % finagleVersion,
  "com.twitter" %% "finagle-thriftmux" % finagleVersion
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

// fork in run := true

// Using sbt-scalariform
scalariformSettings


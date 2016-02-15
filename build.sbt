name := """hanse"""

organization := "com.lvxingpai"

version := "1.0-SNAPSHOT"

val hanse = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"
val morphiaVersion = "1.0.0"
val finagleVersion = "6.30.0"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "com.lvxingpai" %% "core-model" % "0.2.3.25",
  "com.lvxingpai" %% "etcd-store-play" % "0.1.1-SNAPSHOT",
  "com.lvxingpai" %% "morphia-play-injector" % "0.1.3-SNAPSHOT",
  "org.mongodb.morphia" % "morphia" % morphiaVersion,
  "org.mongodb.morphia" % "morphia-validation" % morphiaVersion,
  "com.sksamuel.elastic4s" % "elastic4s-core_2.11" % "2.1.1",
  "com.sksamuel.elastic4s" % "elastic4s-jackson_2.11" % "2.1.1",
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


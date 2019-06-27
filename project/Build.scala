import sbt.Keys._
import sbt.{Resolver, _}

object Common {

  val akkaVersion = "2.5.19"
  val pureConfig  = "0.9.0"

  lazy val commonSettings = Seq(
    scalaVersion := "2.12.5",
    organization := "com.dream",

    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    scalacOptions ++= Seq(
      "-encoding", "UTF-8", // yes, this is 2 args
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-numeric-widen"
    )
  )

  lazy val ServerSettings = Seq(
    resolvers ++= Seq(
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      "javax.inject" % "javax.inject" % "1",
      "joda-time" % "joda-time" % "2.9.9",
      "org.joda" % "joda-convert" % "1.9.2",
      "com.google.inject" % "guice" % "4.1.0",

      "org.julienrf" %% "play-json-derived-codecs" % "4.0.0",
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
      "org.typelevel" %% "cats-core" % "1.4.0",
      "com.beachape" %% "enumeratum" % "1.5.13",
      "com.beachape" %% "enumeratum-play-json" % "1.5.14",
      "com.github.mpilquist" %% "simulacrum" % "0.14.0",
      "org.sisioh" %% "baseunits-scala" % "0.1.21",

      "com.github.pureconfig" %% "pureconfig" % pureConfig,

      "de.heikoseeberger" %% "akka-log4j" % "1.6.1",

      "org.slf4j" % "slf4j-api" % "1.7.25",
//
//      "com.typesafe.play" %% "play-slick" % "4.0.2"

//      "com.mohiva" %% "play-silhouette" % "5.0.1",
//      "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.1",
//      "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.1",
//      "com.mohiva" %% "play-silhouette-persistence" % "5.0.1",
//      "com.mohiva" %% "play-silhouette-testkit" % "5.0.1" % "test",
//      "com.unboundid" % "unboundid-ldapsdk" % "2.3.8" ,
//      "net.codingwell" %% "scala-guice" % "4.1.1",
//      "com.iheart" %% "ficus" % "1.4.2",



    ),
    scalacOptions in Test ++= Seq("-Yrangepos")
  )
}

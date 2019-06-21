import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}



lazy val common =  (project in file("modules/common"))
  .settings(Common.commonSettings)
  .settings(Common.ServerSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % Common.akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % Common.akkaVersion
    )
  )

lazy val ticket =  (project in file("modules/ticket"))
  .settings(Common.commonSettings)
  .settings(Common.ServerSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % Common.akkaVersion,
      "com.typesafe.akka" %% "akka-persistence" % Common.akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % Common.akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % "2.5.19",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.4.0",
      "org.iq80.leveldb" % "leveldb" % "0.7",
      "mysql" % "mysql-connector-java" % "5.1.42",
      "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
    )
  )
  .aggregate(common)
  .dependsOn(common)

lazy val workflow =  (project in file("modules/workflow"))
  .settings(Common.commonSettings)
  .settings(Common.ServerSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % Common.akkaVersion,
      "com.typesafe.akka" %% "akka-persistence" % Common.akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % Common.akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % "2.5.19",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.4.0",
      "org.iq80.leveldb" % "leveldb" % "0.7",
      "mysql" % "mysql-connector-java" % "5.1.42",
      "mysql" % "mysql-connector-java" % "5.1.42",
      "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
    )
  )
  //.aggregate(common, ticket)
  .dependsOn(common, ticket)

lazy val server = (project in file("server"))
  .settings(Common.commonSettings)
  .settings(
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      "com.vmunier" %% "scalajs-scripts" % "1.1.2",
      "net.codingwell" %% "scala-guice" % "4.1.1",
      "com.iheart" %% "ficus" % "1.4.2",

      "com.mohiva" %% "play-silhouette" % "5.0.1",
      "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.1",

      "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.1",
      "com.mohiva" %% "play-silhouette-persistence" % "5.0.1",
      "com.mohiva" %% "play-silhouette-testkit" % "5.0.1" % "test",

      "com.norbitltd" %% "spoiwo" % "1.3.0",

      guice,
      ehcache,
      specs2 % Test
    ),

    // Expose as sbt-web assets some files retrieved from the NPM packages of the `client` project
    npmAssets ++= NpmAssets.ofProject(client) { modules =>
      (modules / "bootstrap").allPaths +++
        (modules / "font-awesome").allPaths
    }.value,

    // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
    EclipseKeys.preTasks := Seq(compile in Compile)
  )
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  //.disablePlugins(PlayLayoutPlugin)
  .aggregate(common, ticket, workflow)
  .dependsOn(sharedJvm, workflow)

lazy val client = (project in file("client"))
  .settings(Common.commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalacOptions ++= Seq("-P:scalajs:sjsDefinedByDefault"),
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % "1.4.0",
      "com.github.japgolly.scalajs-react" %%% "extra" % "1.4.0",
      "com.github.japgolly.scalacss" %%% "ext-react" % "0.5.5",
      "org.scala-js" %%% "scalajs-dom" % "0.9.6",
      "com.lihaoyi" %%% "scalatags" % "0.6.7"
    ),

    npmDependencies in Compile ++= Seq(
      "react" -> "16.7.0",
      "react-dom" -> "16.7.0",
      "jquery" -> "1.9.1",
      "popper.js" -> "^1.14.7",
      "feather-icons" -> "4.22.1",
      "bootstrap" -> "4.3.1",
      "font-awesome" -> "4.7.0"
    )

  ).enablePlugins(ScalaJSPlugin, ScalaJSWeb, ScalaJSBundlerPlugin).
  dependsOn(sharedJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(Common.commonSettings)
  .jvmSettings(
    libraryDependencies ++= Seq(
      "io.suzaku" %% "diode" % "1.1.4"
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "io.suzaku" %%% "diode" % "1.1.4",
      "io.suzaku" %%% "diode-react" % "1.1.4.131"
    )
  )
  .settings(
    libraryDependencies ++= Seq(
      //"org.julienrf" %%% "play-json-derived-codecs" % "4.0.0"
      // logging lib that also works with ScalaJS
      "biz.enef" %%% "slogging" % "0.6.0",
      "com.lihaoyi" %%% "autowire" % "0.2.6",
      //"io.suzaku"                         %%% "boopickle"      % "1.3.0",
      "io.suzaku" %%% "boopickle" % "1.2.6"
    )
  )
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen { s: State => "project server" :: s }

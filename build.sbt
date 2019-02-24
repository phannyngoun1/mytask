import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val server = (project in file("server")).settings(commonSettings).settings(
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.1.2",
    guice,
    specs2 % Test
  ),
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  EclipseKeys.preTasks := Seq(compile in Compile)
).enablePlugins(PlayScala, WebScalaJSBundlerPlugin).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(commonSettings).settings(
  scalaJSUseMainModuleInitializer := true,
  libraryDependencies ++= Seq(
    "com.github.japgolly.scalajs-react" %%% "core" % "1.4.0",
    "com.github.japgolly.scalajs-react" %%% "extra" % "1.4.0",
    "com.github.japgolly.scalacss" %%% "ext-react" % "0.5.5",
    "org.scala-js" %%% "scalajs-dom" % "0.9.6",
    "com.lihaoyi" %%% "scalatags" % "0.6.7",

//    "io.suzaku" %%% "diode-core" % "1.1.3",
//    "io.suzaku" %%% "diode-react" % "1.1.3",
  ),

  npmDependencies in Compile ++= Seq(
    "react" -> "16.7.0",
    "react-dom" -> "16.7.0")

).enablePlugins(ScalaJSPlugin, ScalaJSWeb, ScalaJSBundlerPlugin).
  dependsOn(sharedJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings)
  .jvmSettings(
    libraryDependencies ++= Seq(
      "io.suzaku" %% "diode" % "1.1.4"
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "io.suzaku" %%% "diode" % "1.1.4",
      // https://mvnrepository.com/artifact/io.suzaku/diode-react
      "io.suzaku" %%% "diode-react" % "1.1.4.131"
    )
    /* ... */
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

lazy val commonSettings = Seq(
  scalaVersion := "2.12.5",
  organization := "com.dream"
)

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen {s: State => "project server" :: s}

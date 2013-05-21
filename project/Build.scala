import sbt._
import Keys._
import sbtassembly.Plugin._

object Build extends sbt.Build {

  val projectVersion = "0.1-SNAPSHOT"
  val akkaVersion = "2.1.4"
  val scalazVersion = "7.0.0"
  val sprayVersion = "1.1-M7"

  lazy val buildSettings = Defaults.defaultSettings ++ seq(ReflectPlugin.allSettings:_*) ++
    org.scalastyle.sbt.ScalastylePlugin.Settings ++ Seq(
    version := projectVersion,
    organization := "com.lucho",
    scalaVersion := "2.10.1",
    resolvers ++= Seq(
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      "spray repo" at "http://repo.spray.io/",
      "spray nightlies repo" at "http://nightlies.spray.io/",
      "Mark Schaake" at "http://markschaake.github.com/snapshots"
    ),
    logLevel := Level.Info,

    // only show warnings and errors on the screen for compilations.
    //  this applies to both test:compile and compile and is Info by default
    logLevel in compile := Level.Info,
    scalacOptions := Seq("-unchecked", "-encoding", "utf8"),

    mainClass := Some("com.lucho.Main"),

    libraryDependencies ++= Seq(
      "io.spray"                 %   "spray-can"         % sprayVersion,
      "io.spray"                 %   "spray-routing"     % sprayVersion,
      "io.spray"                 %   "spray-testkit"     % sprayVersion,
      "io.spray"                 %   "spray-client"      % sprayVersion,
      "io.spray"                 %%  "spray-json"        % "1.2.4",
      "io.spray"                 %%  "twirl-api"         % "0.6.1",
      "ch.qos.logback"           %   "logback-classic"   % "1.0.1",
      "joda-time"                %   "joda-time"         % "2.1",
      "org.joda"                 %   "joda-convert"      % "1.3",
      "org.reactivemongo"        %%  "reactivemongo"     % "0.9" exclude("org.scala-stm", "scala-stm_2.10.0-"),
      "com.typesafe.akka"        %%  "akka-actor"        % akkaVersion,
      "com.typesafe.akka"        %%  "akka-dataflow"     % akkaVersion,
      "com.typesafe.akka"        %%  "akka-kernel"       % akkaVersion,
      "com.typesafe.akka"        %%  "akka-camel"        % akkaVersion,
      "com.typesafe.akka"        %%  "akka-transactor"   % akkaVersion,
      "com.typesafe.akka"        %%  "akka-slf4j"        % akkaVersion,
      "com.typesafe.akka"        %%  "akka-zeromq"       % akkaVersion,
      "org.scalaz"               %% "scalaz-core"        % scalazVersion,
      "org.scalaz"               %% "scalaz-typelevel"   % scalazVersion,
      "com.chuusai"              %% "shapeless"          % "1.2.4",
      "org.spire-math"           %% "spire"              % "0.3.0"
    ),

    shellPrompt := ShellPrompt.buildShellPrompt,

    ReflectPlugin.reflectPackage := "com.lucho",
    ReflectPlugin.reflectClass := "Project",
    sourceGenerators in Compile <+= ReflectPlugin.reflect map identity
  )


  lazy val root = Project(id = "spray-sample", base = file("."), settings = buildSettings ++ assemblySettings) settings(
    net.virtualvoid.sbt.graph.Plugin.graphSettings: _*
  )

  // Shell prompt which show the current project,
  // git branch and build version
  object ShellPrompt {
    object devnull extends ProcessLogger {
      def info (s: => String) {}
      def error (s: => String) { }
      def buffer[T] (f: => T): T = f
    }
    def currBranch = (
      ("git status -sb" lines_! devnull headOption)
        getOrElse "-" stripPrefix "## "
      )

    val buildShellPrompt = {
      (state: State) => {
        val currProject = Project.extract (state).currentProject.id
        "%s:%s:%s> ".format (
          currProject, currBranch, projectVersion
        )
      }
    }
  }

}

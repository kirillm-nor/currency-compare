import Library._
import net.virtualvoid.sbt.graph.DependencyGraphSettings.graphSettings

ThisBuild / scalaVersion := "2.13.0"
ThisBuild / organization := "io.kirmit"
ThisBuild / version := "0.1"

lazy val root = (project in file("."))
  .settings(
    name := "CurrencyCompare",
    resolvers += Resolver.mavenLocal
  )
  .aggregate(service, loadTests)

lazy val service = (project in file("compare-service"))
  .settings(
    name := "compare-service",
    buildInfoKeys ++= Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      sbtVersion,
      "commit" -> {
        git.gitHeadCommit.value
      }
    ),
    buildInfoPackage := organization.value,
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoOptions += BuildInfoOption.ToJson,
    scalacOptions ++= Seq("-encoding",
                          "UTF-8",
                          s"-target:jvm-${Version.jdk}",
                          "-feature",
                          "-language:_",
                          "-deprecation",
                          "-unchecked",
                          "-Xlint:-missing-interpolator"),
    javacOptions in Compile ++= Seq("-encoding",
                                    "UTF-8",
                                    "-source",
                                    Version.jdk,
                                    "-target",
                                    Version.jdk,
                                    "-Xlint:deprecation",
                                    "-Xlint:unchecked",
                                    "-Xfatal-warnings"),
    fork in run := true,
    javaAgents += "io.kamon" % "kanela-agent" % "1.0.1",
    libraryDependencies ++= Seq(
      akkaActor,
      akkaStream,
      akkaHttpCore,
      akkaHttp,
      circeCore,
      circeGeneric,
      circeParser,
      kamonCore,
      kamonAkka,
      kamonAkkaHttp,
      kamonSystem,
      kamonFuture,
      kamonStatus,
      scalaTest,
      akkaTestkit,
      akkaStreamTestkit
    ),
  )
  .settings(graphSettings)
  .settings(Revolver.settings)
  .enablePlugins(JavaServerAppPackaging, DockerPlugin, JavaAgent)
  .enablePlugins(BuildInfoPlugin, GitVersioning, GitBranchPrompt)

lazy val loadTests = (project in file("load-tests"))
  .settings(
    name := "load-tests",
    libraryDependencies ++= Seq(gatlingCharts, gatlingTest)
  )

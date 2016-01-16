import de.heikoseeberger.sbtheader.license.Apache2_0

enablePlugins(AutomateHeaderPlugin, GitVersioning, GitBranchPrompt)
sbtPlugin := true

git.baseVersion := "0.2.0"
organization := "de.knutwalker"

name := "sbt-knutwalker"
description := "Opinionated meta plugin for github based open source projects"

licenses := List("Apache 2.0" → url("https://www.apache.org/licenses/LICENSE-2.0.html"))
headers := Map("scala" -> Apache2_0("2015", "Paul Horn"))

scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfuture",
  "-Xfatal-warnings",
  "-language:_",
  "-target:jvm-1.6",
  "-encoding", "UTF-8")
libraryDependencies ++= List(
  "org.specs2"                 %% "specs2-core"               % "3.5"    % "test",
  "org.specs2"                 %% "specs2-scalacheck"         % "3.5"    % "test",
  "org.scalacheck"             %% "scalacheck"                % "1.12.2" % "test",
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.12" % "0.1.1"  % "test",
  "org.typelevel"              %% "scalaz-specs2"             % "0.4.0"  % "test"
    exclude("org.specs2", s"specs2-core${scalaBinaryVersion.value}")
    exclude("org.specs2", s"specs2-scalacheck${scalaBinaryVersion.value}"))

addSbtPlugin("org.scoverage"     % "sbt-coveralls" % "1.0.0.BETA1")
addSbtPlugin("de.heikoseeberger" % "sbt-header"    % "1.5.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-pgp"       % "0.8.3")
addSbtPlugin("com.github.gseitz" % "sbt-release"   % "0.8.5")
addSbtPlugin("org.scoverage"     % "sbt-scoverage" % "1.0.4")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"  % "0.2.2")

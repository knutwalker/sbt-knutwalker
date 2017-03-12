import de.heikoseeberger.sbtheader.license.Apache2_0

enablePlugins(AutomateHeaderPlugin, GitVersioning, GitBranchPrompt)
sbtPlugin := true

git.baseVersion := "0.5.1"
organization := "de.knutwalker"

name := "sbt-knutwalker"
description := "Opinionated meta plugin for github based open source projects"

licenses := List("Apache 2.0" → url("https://www.apache.org/licenses/LICENSE-2.0.html"))
headers := Map("scala" -> Apache2_0("2015 – 2017", "Paul Horn"))

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
  "org.specs2"                 %% "specs2-core"               % "3.8.9"  % "test",
  "org.specs2"                 %% "specs2-scalacheck"         % "3.8.9"  % "test",
  "org.scalacheck"             %% "scalacheck"                % "1.5"    % "test",
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.4"  % "test",
  "org.typelevel"              %% "scalaz-specs2"             % "0.5.0"  % "test"
    exclude("org.specs2", s"specs2-core${scalaBinaryVersion.value}")
    exclude("org.specs2", s"specs2-scalacheck${scalaBinaryVersion.value}"))


addSbtPlugin("com.eed3si9n"       % "sbt-assembly"    % "0.14.4")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo"   % "0.6.1")
addSbtPlugin("se.marcuslonnberg"  % "sbt-docker"      % "1.4.1")
addSbtPlugin("com.typesafe.sbt"   % "sbt-ghpages"     % "0.6.0")
addSbtPlugin("com.typesafe.sbt"   % "sbt-git"         % "0.8.5")
addSbtPlugin("de.heikoseeberger"  % "sbt-header"      % "1.8.0")
addSbtPlugin("pl.project13.scala" % "sbt-jmh"         % "0.2.21")
addSbtPlugin("com.typesafe"       % "sbt-mima-plugin" % "0.1.14")
addSbtPlugin("com.typesafe.sbt"   % "sbt-site"        % "1.2.0")
addSbtPlugin("com.jsuereth"       % "sbt-pgp"         % "1.0.0")
addSbtPlugin("com.github.gseitz"  % "sbt-release"     % "1.0.4")
addSbtPlugin("io.spray"           % "sbt-revolver"    % "0.8.0")
addSbtPlugin("org.tpolecat"       % "tut-plugin"      % "0.4.2")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"   % "1.5.0")
addSbtPlugin("org.xerial.sbt"     % "sbt-sonatype"    % "1.1")
addSbtPlugin("com.eed3si9n"       % "sbt-unidoc"      % "0.4.0")

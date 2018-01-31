
enablePlugins(AutomateHeaderPlugin, GitVersioning, GitBranchPrompt)
sbtPlugin := true

val sbtVersion_ = "1.1.0"
sbtVersion := sbtVersion_
crossSbtVersions := List(sbtVersion_)
scalaVersion := "2.12.4"
git.baseVersion := "0.5.1"
organization := "de.knutwalker"

name := "sbt-knutwalker"
description := "Opinionated meta plugin for github based open source projects"

headerLicense := Some(HeaderLicense.ALv2("2015 - 2017", "Paul Horn"))

scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfuture",
  "-Xfatal-warnings",
  "-language:_",
  "-encoding", "UTF-8")
libraryDependencies ++= List(
  "org.specs2"                 %% "specs2-core"               % "4.0.2"  % "test",
  "org.specs2"                 %% "specs2-scalacheck"         % "4.0.2"  % "test",
  "org.scalacheck"             %% "scalacheck"                % "1.13.5" % "test",
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.8"  % "test",
  "org.typelevel"              %% "scalaz-specs2"             % "0.5.2"  % "test"
    exclude("org.specs2", s"specs2-core${scalaBinaryVersion.value}")
    exclude("org.specs2", s"specs2-scalacheck${scalaBinaryVersion.value}"))


addSbtPlugin("com.eed3si9n"       % "sbt-assembly"    % "0.14.6")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo"   % "0.7.0")
addSbtPlugin("se.marcuslonnberg"  % "sbt-docker"      % "1.4.1")
addSbtPlugin("com.typesafe.sbt"   % "sbt-ghpages"     % "0.6.2")
addSbtPlugin("com.typesafe.sbt"   % "sbt-git"         % "0.9.3")
addSbtPlugin("de.heikoseeberger"  % "sbt-header"      % "4.1.0")
addSbtPlugin("pl.project13.scala" % "sbt-jmh"         % "0.3.3")
addSbtPlugin("com.typesafe"       % "sbt-mima-plugin" % "0.1.18")
addSbtPlugin("com.typesafe.sbt"   % "sbt-site"        % "1.3.1")
addSbtPlugin("com.jsuereth"       % "sbt-pgp"         % "1.1.0")
addSbtPlugin("com.github.gseitz"  % "sbt-release"     % "1.0.7")
addSbtPlugin("io.spray"           % "sbt-revolver"    % "0.9.1")
addSbtPlugin("org.tpolecat"       % "tut-plugin"      % "0.6.2")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"   % "1.5.1")
addSbtPlugin("org.xerial.sbt"     % "sbt-sonatype"    % "2.1")
addSbtPlugin("com.eed3si9n"       % "sbt-unidoc"      % "0.4.1")

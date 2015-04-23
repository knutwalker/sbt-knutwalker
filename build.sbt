sbtPlugin := true

name := "sbt-knutwalker"
organization := "de.knutwalker"
version := "0.1.0-SNAPSHOT"

licenses := List("Apache 2" -> url("https://www.apache.org/licenses/LICENSE-2.0.html"))

// resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("org.scoverage"     % "sbt-coveralls" % "1.0.0.BETA1")
addSbtPlugin("de.heikoseeberger" % "sbt-header"    % "1.4.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-pgp"       % "0.8.3")
addSbtPlugin("com.github.gseitz" % "sbt-release"   % "0.8.5")
addSbtPlugin("org.scoverage"     % "sbt-scoverage" % "1.0.4")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"  % "0.2.2")

publishMavenStyle := false

enablePlugins(AutomateHeaderPlugin)

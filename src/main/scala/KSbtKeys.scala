package de.knutwalker.sbt

import sbt._

trait KSbtKeys {

  lazy val maintainer =
    settingKey[String]("The Maintainer that appears in the license header")

  lazy val githubProject =
    settingKey[Github]("Github user/repo for this project")

  lazy val githubDevs =
    settingKey[Seq[Developer]]("Developers of this project")

  lazy val releaseThis =
    settingKey[Boolean]("True if this project should be released")

  lazy val projectName =
    settingKey[String]("umbrella project name")

  lazy val akkaVersion =
    settingKey[String]("Version of Akka")

  lazy val luceneVersion =
    settingKey[String]("Version of Lucene")

  lazy val nettyVersion =
    settingKey[String]("Version of Netty")

  lazy val rxJavaVersion =
    settingKey[String]("Version of RxJava")

  lazy val rxScalaVersion =
    settingKey[String]("Version of RxScala")

  lazy val shapelessVersion =
    settingKey[String]("Version of Shapeless")

  lazy val scalazVersion =
    settingKey[String]("Version of Scalaz")

}
object KSbtKeys extends KSbtKeys

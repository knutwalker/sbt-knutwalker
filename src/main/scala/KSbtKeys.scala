/*
 * Copyright 2015 Paul Horn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  lazy val scalacFlags =
    settingKey[ScalacOptions]("scalac options in a typely manner")

  lazy val scalaMainVersion =
    settingKey[ScalaMainVersion]("scala binary version in a typely manner")

  lazy val javaVersion =
    settingKey[JavaVersion]("Java version (target)")

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

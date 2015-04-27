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

  lazy val libraries =
    settingKey[Seq[Library]]("A set of libraries to use")
}
object KSbtKeys extends KSbtKeys

/*
 * Copyright 2015 - 2017 Paul Horn
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

  lazy val projectName =
    settingKey[String]("umbrella project name")

  lazy val scalaMainVersion =
    settingKey[ScalaMainVersion]("scala binary version in a typely manner")

  lazy val javaVersion =
    settingKey[JavaVersion]("Java version (target)")

  lazy val experimentalJava8Support =
    settingKey[Boolean]("Whether to enable experimental flags that make Scala interop with Java 8")

  lazy val latestVersionTag =
    settingKey[Option[String]]("The latest tag describing a version number")

  lazy val latestVersion =
    settingKey[String]("The latest version or the current one, if there is no previous version")

  lazy val genModules =
    taskKey[Seq[(File, String)]]("Generate module files for a tut-guide")

  lazy val makeReadme =
    taskKey[Option[File]]("Generate readme file from tutorial")

  lazy val commitReadme =
    taskKey[Option[File]]("Commit the readme file")

  lazy val buildReadmeContent =
    taskKey[Seq[(File, String)]]("Generate content for the readme file")

  lazy val readmeFile =
    settingKey[File]("The readme file to build")

  lazy val readmeCommitMessage =
    settingKey[String]("The message to commit the readme file with")

  lazy val applicationJvmHeap =
    settingKey[String]("If run as application, how much heap to start with (e.g. 1g)")

  lazy val applicationPorts =
    settingKey[Seq[Int]]("If run as application, which ports are exposed")

  lazy val applicationJavaOpts =
    settingKey[Seq[String]]("If run as application, what are some default jvm arguments")
}
object KSbtKeys extends KSbtKeys

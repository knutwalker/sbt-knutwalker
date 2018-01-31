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

import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.{ Version, versionFormatError }


object KReleaseSteps {

  lazy val inquireVersions = ReleaseStep { st: State ⇒
    val extracted = Project.extract(st)
    val lastVer = extracted.get(KSbtKeys.latestVersionTag).getOrElse("0.0.0")
    val (newState, bump) = extracted.runTask(releaseVersionBump, st)
    val suggestedVersion = Version(lastVer).map(_.withoutQualifier.bump(bump).string).getOrElse(versionFormatError)
    val releaseVersion = readVersion(suggestedVersion, "Release version [%s] : ")
    newState.put(ReleaseKeys.versions, (releaseVersion, releaseVersion))
  }

  lazy val setReleaseVersion = ReleaseStep { st: State =>
    val vs = st.get(ReleaseKeys.versions).getOrElse(sys.error("No versions are set! Was this release part executed before inquireVersions?"))
    val selected = vs._1
    st.log.info(s"Setting version to '$selected'.")
    reapply(Seq(version in ThisBuild := selected), st)
  }

  lazy val publishSignedArtifacts = ReleaseStep(
    action = releaseStepCommand("publishSigned"),
    enableCrossBuild = true
  )

  lazy val releaseToCentral = ReleaseStep(
    action = releaseStepCommand("sonatypeReleaseAll"),
    enableCrossBuild = true
  )

  lazy val pushGithubPages = ReleaseStep(
    action = optionallyProcess("docs/ghpagesPushSite", _),
    enableCrossBuild = false
  )

  lazy val commitTheReadme = ReleaseStep(
    action = optionallyProcess("docs/commitReadme", _),
    enableCrossBuild = false
  )


  private def readVersion(ver: String, prompt: String): String = {
    SimpleReader.readLine(prompt format ver) match {
      case Some("") => ver
      case Some(input) => Version(input).map(_.string).getOrElse(versionFormatError)
      case None => sys.error("No version provided!")
    }
  }

  private def optionallyProcess(command: String, state: State): State = {
    val parser = Command.combine(state.definedCommands)
    DefaultParsers.parse(command, parser(state)) match {
      case Right(s)     ⇒
        s() // apply command.  command side effects happen here
      case Left(errMsg) ⇒
        state.log.debug(errMsg)
        state
    }
  }
}

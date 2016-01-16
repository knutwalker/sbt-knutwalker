/*
 * Copyright 2015 – 2016 Paul Horn
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

import com.typesafe.sbt.SbtGit.GitKeys
import com.typesafe.sbt.git.JGit
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.{ Version, versionFormatError }


object KReleaseSteps {

  lazy val inquireVersions = ReleaseStep { st: State ⇒
    val extracted = Project.extract(st)
    val jgit = extracted.get(GitKeys.gitReader).asInstanceOf[JGit]
    val lastVer = KCode.findLatestVersion(jgit).getOrElse("0.0.0")
    val bump = extracted.get(releaseVersionBump)
    val suggestedVersion = Version(lastVer).map(_.withoutQualifier.bump(bump).string).getOrElse(versionFormatError)
    val releaseVersion = readVersion(suggestedVersion, "Release version [%s] : ")
    st.put(ReleaseKeys.versions, (releaseVersion, releaseVersion))
  }

  lazy val setReleaseVersion = ReleaseStep { st: State =>
    val vs = st.get(ReleaseKeys.versions).getOrElse(sys.error("No versions are set! Was this release part executed before inquireVersions?"))
    val selected = vs._1
    st.log.info(s"Setting version to '$selected'.")
    reapply(Seq(version in ThisBuild := selected), st)
  }

  lazy val publishSignedArtifacts = ReleaseStep(
    action = Command.process("publishSigned", _),
    enableCrossBuild = true
  )

  lazy val releaseToCentral = ReleaseStep(
    action = Command.process("sonatypeReleaseAll", _),
    enableCrossBuild = true
  )

  lazy val pushGithubPages = ReleaseStep(
    action = Command.process("docs/ghpagesPushSite", _),
    enableCrossBuild = false
  )

  lazy val commitTheReadme = ReleaseStep(
    action = Command.process("docs/commitReadme", _),
    enableCrossBuild = false
  )


  private def readVersion(ver: String, prompt: String): String = {
    SimpleReader.readLine(prompt format ver) match {
      case Some("") => ver
      case Some(input) => Version(input).map(_.string).getOrElse(versionFormatError)
      case None => sys.error("No version provided!")
    }
  }
}

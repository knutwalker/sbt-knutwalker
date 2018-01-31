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

import sbt.{Project, State}
import sbtrelease.Version
import com.typesafe.sbt.git.JGit
import de.heikoseeberger.sbtheader.License
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.HeaderLicense

import scala.xml.{Node => XNode, NodeSeq => XNodeSeq}
import scala.xml.transform.{RewriteRule, RuleTransformer}

object KCode extends VersionOrdering {
  private[this] val removeScoverageFromPom = new RewriteRule {
    override def transform(n: XNode): XNodeSeq =
      if (n.label == "dependency" && (n \ "scope").text == "provided" && (n \ "groupId").text == "org.scoverage")
        XNodeSeq.Empty
      else n
  }
  val removeScoverage =
    new RuleTransformer(removeScoverageFromPom)


  def headerConfig(year: Option[Int], maintainer: String): Option[License] = {
    val thisYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = List(year.getOrElse(thisYear), thisYear).distinct.mkString(" – ")
    Some(HeaderLicense.ALv2(years, maintainer))
  }

  def sbtDevelopers(sbtv: String, devs: Seq[Developer]): List[sbt.Developer] = {
    if (checkSbtVersionIsAtMost(sbtv, 0, 13, 8)) Nil
    else devs.map(_.sbtDev).toList
  }

  def makePomExtra(sbtv: String, devs: Seq[Developer], gh: Option[Github]) =
    makePomDevs(sbtv, devs) ++ makePomApiUrl(gh)

  def makePomDevs(sbtv: String, devs: Seq[Developer]) = {
    if (devs.isEmpty || checkSbtVersionIsGreaterThan(sbtv, 0, 13, 8))
      XNodeSeq.Empty
    else
      <developers>{devs.map { d =>
        <developer>
          <id>{d.id}</id>
          <name>{d.name}</name>
          <url>{d.url}</url>
        </developer>
      }}</developers>
  }

  def makePomApiUrl(gh: Option[Github]) = gh match {
    case Some(Github(org, repo)) ⇒
      <properties>
        <info.apiURL>http://{org}.github.io/{repo}/api/</info.apiURL>
      </properties>
    case None ⇒
      XNodeSeq.Empty
  }

  def configurePrompt(st: State) = {
    import scala.Console._
    val name = Project.extract(st).currentRef.project
    val color = GREEN
    (if (name == "parent") "" else s"[$color$name$RESET] ") + "> "
  }

  def findLatestVersion(git: JGit): Option[String] = {
    val tags = git.tags.collect {
      case tag if tag.getName startsWith "refs/tags/" ⇒
        tag.getName drop 10 replaceFirst ("^v", "")
    }
    val sortedTags = tags.flatMap(Version(_)).sorted.map(_.string)
    sortedTags.lastOption
  }





  private def checkSbtVersionIsAtMost(sbtv: String, major: Int, minor: Int, bugfix: Int): Boolean = {
    val sbt = Version(sbtv)
    sbt.exists(v ⇒ v.major == major &&
                   v.subversions.headOption.exists(_ <= minor) &&
                   v.subversions.drop(1).headOption.exists(_ <= bugfix))
  }

  private def checkSbtVersionIsGreaterThan(sbtv: String, major: Int, minor: Int, bugfix: Int): Boolean =
    !checkSbtVersionIsAtMost(sbtv, major, minor, bugfix)
}

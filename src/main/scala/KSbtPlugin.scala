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
import sbt.Keys._
import scala.xml.{Node => XNode, NodeSeq => XNodeSeq}
import scala.xml.transform.{RewriteRule, RuleTransformer}
import de.heikoseeberger.sbtheader.HeaderPlugin
import de.heikoseeberger.sbtheader.license.Apache2_0
import org.scoverage.coveralls.CoverallsPlugin
import scoverage.ScoverageSbtPlugin
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.Version
import xerial.sbt.Sonatype.sonatypeSettings

import ScalacOptions._

// TODO:
//  - versions like scalazVersion
//    - adding resolvers and libraryDependencies
//    - configurable with module lists (e.g. "core", "effect", ...)
//  - make releasesteps configurable
object KSbtPlugin extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = plugins.JvmPlugin && HeaderPlugin && ScoverageSbtPlugin && CoverallsPlugin

  object autoImport extends KSbtKeys {
    lazy val dontRelease: Seq[Def.Setting[_]] = List(
              publish := (),
         publishLocal := (),
      publishArtifact := false
    )

    type Github = de.knutwalker.sbt.Github
    val Github = de.knutwalker.sbt.Github

    type Developer = de.knutwalker.sbt.Developer
    val Developer = de.knutwalker.sbt.Developer

    type ScalacOptions = de.knutwalker.sbt.ScalacOptions
    val ScalacOptions = de.knutwalker.sbt.ScalacOptions

    type ScalaMainVersion = de.knutwalker.sbt.ScalaMainVersion
    val ScalaMainVersion = de.knutwalker.sbt.ScalaMainVersion

    type JavaVersion = de.knutwalker.sbt.JavaVersion
    val JavaVersion = de.knutwalker.sbt.JavaVersion
  }

  import autoImport._

  override lazy val projectSettings =
   ksbtSettings ++
   inConfig(Compile)(headerSettings) ++
   inConfig(Test)(headerSettings)

  lazy val ksbtSettings: Seq[Def.Setting[_]] = List(
          maintainer := organizationName.value,
          githubDevs := githubProject.?.value.map(gh ⇒ Developer(gh.org, maintainer.value)).toList,
         releaseThis := true,
    scalaMainVersion := ScalaMainVersion(scalaBinaryVersion.value),
         javaVersion := JavaVersion.Java18,
         scalacFlags := {
           Lint and GoodMeasure and SimpleWarnings and Utf8 and LanguageFeature.Existentials and
             LanguageFeature.HigherKinds and LanguageFeature.ImplicitConversions and
             EliminateDeadCode and DisallowInferredAny and DisallowAdaptedArgs and
             DisallowNumericWidening
         }
  ) ++ derivedSettings ++ compilerSettings ++ pomRelatedSettings ++ publishSettings

  lazy val derivedSettings: Seq[Def.Setting[_]] = List(
    organizationHomepage := githubProject.?.value.map(_.organization),
                homepage := githubProject.?.value.map(_.repository),
             shellPrompt := { state => configurePrompt(state) },
             logBuffered := false,
   libraryDependencies <++= (scalaBinaryVersion, akkaVersion.?, luceneVersion.?, nettyVersion.?, rxJavaVersion.?, rxScalaVersion.?, shapelessVersion.?, scalazVersion.?) apply addLibrary,
             resolvers <++= scalazVersion.? apply addResolvers,
         cleanKeepFiles ++= List("resolution-cache", "streams").map(target.value / _),
           updateOptions ~= (_.withCachedResolution(cachedResoluton = true))
  )

  lazy val pomRelatedSettings: Seq[Def.Setting[_]] = List(
           scmInfo := githubProject.?.value.map(_.scmInfo),
          licenses := List("Apache 2" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
        developers := sbtDevelopers(sbtVersion.value, githubDevs.value),
    pomPostProcess := { (node) => rewriteTransformer.transform(node).head },
          pomExtra := pomExtra.value ++ makePomExtra(sbtVersion.value, githubDevs.value)
  )

  lazy val compilerSettings: Seq[Def.Setting[_]] = List(
               scalacOptions in Compile := ScalacOptions(scalacFlags.value, scalaMainVersion.value, javaVersion.value),
    scalacOptions in (Compile, console) ~= (_ filterNot (x => x == "-Xfatal-warnings" || x.startsWith("-Ywarn"))),
       scalacOptions in (Test, console) ~= (_ filterNot (x => x == "-Xfatal-warnings" || x.startsWith("-Ywarn"))),
                  scalacOptions in Test += "-Yrangepos"
  )

  lazy val headerSettings: Seq[Def.Setting[_]] = {
    import HeaderPlugin.autoImport.{headers, createHeaders}
    List(
      compile := compile.dependsOn(createHeaders).value,
      headers <<= (startYear, maintainer) apply headerConfig
    )
  }

  lazy val publishSettings: Seq[Def.Setting[_]] =
    ReleasePlugin.projectSettings ++ sonatypeSettings ++ List(
              //        publish <<= releaseThis map { r => if (r) publish.value else () },
              //   publishLocal <<= releaseThis map { r => if (r) publishLocal.value else () },
              // publishArtifact := releaseThis.value,
      publishArtifact in Test := false,
            releaseTagComment := s"Release version ${version.value}",
         releaseCommitMessage := s"Set version to ${version.value}",
           releaseVersionBump := Version.Bump.Bugfix,
               releaseProcess := List[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runClean,
        runTest,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        publishSignedArtifacts,
        releaseToCentral,
        setNextVersion,
        commitNextVersion,
        pushChanges,
        publishArtifacts
      )
    )

  private def headerConfig(year: Option[Int], maintainer: String) = {
    val thisYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = List(year.getOrElse(thisYear), thisYear).distinct.mkString(" – ")
    Map("java"  -> Apache2_0(years, maintainer),
        "scala" -> Apache2_0(years, maintainer),
        "conf"  -> Apache2_0(years, maintainer, "#"))
  }

  def sbtDevelopers(sbtv: String, devs: Seq[Developer]): List[sbt.Developer] = {
    if (checkSbtVersionIsAtMost(sbtv, 0, 13, 8)) Nil
    else devs.map(_.sbtDev).toList
  }

  def makePomExtra(sbtv: String, devs: Seq[Developer]) = {
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

  def checkSbtVersionIsAtMost(sbtv: String, major: Int, minor: Int, bugfix: Int): Boolean = {
    val sbt = Version(sbtv)
    sbt.exists(v ⇒ v.major == major && v.minor.exists(_ <= minor) && v.bugfix.exists(_ <= bugfix))
  }

  def checkSbtVersionIsGreaterThan(sbtv: String, major: Int, minor: Int, bugfix: Int): Boolean =
    !checkSbtVersionIsAtMost(sbtv, major, minor, bugfix)

  private def addLibrary(cross: String,
      akka: Option[String],
      lucene: Option[String],
      netty: Option[String],
      rxJava: Option[String],
      rxScala: Option[String],
      shapeless: Option[String],
      scalaz: Option[String]) = {

    akka.toList.flatMap(Dependencies.akka) ++
    lucene.toList.flatMap(Dependencies.lucene) ++
    netty.toList.flatMap(Dependencies.netty) ++
    rxJava.toList.flatMap(Dependencies.rxJava) ++
    rxScala.toList.flatMap(Dependencies.rxScala) ++
    shapeless.toList.flatMap(Dependencies.shapeless) ++
    scalaz.toList.flatMap(Dependencies.scalaz)
  }

  private def addResolvers(scalaz: Option[String]) =
    scalaz match {
      case Some(_) => List("Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases")
      case None    => Nil
    }

  private def configurePrompt(st: State) = {
    import scala.Console._
    val name = Project.extract(st).currentRef.project
    val color = GREEN
    (if (name == "parent") "" else s"[$color$name$RESET] ") + "> "
  }

  private val rewriteRule = new RewriteRule {
    override def transform(n: XNode): XNodeSeq =
      if (n.label == "dependency" && (n \ "scope").text == "provided" && (n \ "groupId").text == "org.scoverage")
        XNodeSeq.Empty
      else n
  }

  private val rewriteTransformer = new RuleTransformer(rewriteRule)

  private lazy val publishSignedArtifacts = ReleaseStep(
    action = Command.process("publishSigned", _),
    enableCrossBuild = true
  )

  private lazy val releaseToCentral =ReleaseStep(
    action = Command.process("sonatypeReleaseAll", _),
    enableCrossBuild = true
  )
}

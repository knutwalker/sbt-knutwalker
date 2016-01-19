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

import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys.ghpagesNoJekyll
import com.typesafe.sbt.SbtGhPages.ghpages
import com.typesafe.sbt.SbtGit
import com.typesafe.sbt.SbtGit.{ GitKeys, git }
import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.sbt.SbtSite.site
import com.typesafe.sbt.git.JGit
import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifacts
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.{headers, createHeaders}
import sbtassembly.AssemblyKeys._
import sbtdocker.DockerKeys._
import sbtdocker.{ ImageName, Dockerfile }
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.Version
import sbtunidoc.Plugin.UnidocKeys._
import sbtunidoc.Plugin.{ ScalaUnidoc, unidocSettings }
import spray.revolver.RevolverPlugin.autoImport.{reStart, reStop, reStatus}
import tut.Plugin._


object KSbtPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = plugins.JvmPlugin

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

    type ScalaMainVersion = de.knutwalker.sbt.ScalaMainVersion
    val ScalaMainVersion = de.knutwalker.sbt.ScalaMainVersion

    type JavaVersion = de.knutwalker.sbt.JavaVersion
    val JavaVersion = de.knutwalker.sbt.JavaVersion

    lazy val RunDebug = config("debug") extend Runtime

    def tutsSettings(projects: ProjectReference*) =
      unidocSettings ++ site.settings ++ ghpages.settings ++ tutSettings ++ dontRelease ++ Seq(
        tutSourceDirectory := sourceDirectory.value / "tut",
        buildReadmeContent := tut.value,
        readmeFile := baseDirectory.value / ".." / "README.md",
        readmeCommitMessage := "Update README",
        unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(projects: _*),
        site.addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), "api"),
        site.addMappingsToSiteDir(tut, "tut"),
        site.addMappingsToSiteDir(genModules, "_data"),
        ghpagesNoJekyll := false,
        scalacOptions in (ScalaUnidoc, unidoc) ++= Seq(
          "-doc-source-url", scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
          "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath,
          "-doc-title", githubProject.value.repo,
          "-doc-version", version.value,
          "-diagrams",
          "-groups"
        ),
        git.remoteRepo := githubProject.value.remoteSsh,
        includeFilter in makeSite ~= (_ || "*.yml" || "*.md" || "*.scss"),
        tutScalacOptions ~= (_.filterNot(Set("-Xfatal-warnings", "-Ywarn-unused-import", "-Ywarn-dead-code"))),
        watchSources <++= (tutSourceDirectory, siteSourceDirectory, includeFilter in makeSite) map { (t, s, f) ⇒ (t ** "*.md").get ++ (s ** f).get }
      )

    def parentSettings(additional: SettingsDefinition*): Seq[Def.Setting[_]] = (Seq(
      name                        := projectName.value,
      applicationPorts            := Seq(),
      applicationJavaOpts         := Seq(),
      aggregate in assembly       := false,
      aggregate in reStart        := false,
      aggregate in reStop         := false,
      aggregate in reStatus       := false,
      assemblyJarName in assembly := { if (isSnapshot.value) s"${projectName.value}.jar" else s"${projectName.value}-${version.value}.jar" },
      assemblyOption in assembly  := {
       val mem = applicationJvmHeap.?.value.getOrElse("1g")
       val args = (applicationJavaOpts.value ++ Seq(s"-Xms$mem", s"-Xmx$mem")).mkString(" ")
       val prev = (assemblyOption in assembly).value
       prev.copy(prependShellScript = Some(Seq("#!/usr/bin/env sh", s"exec java $args -jar" + """ "$0" "$@"""")))
     }
    ) ++ additional).flatMap(_.settings)

    def dockerSettings(additional: Def.Setting[_]*): SettingsDefinition = Seq(
      name                         := projectName.value,
      applicationPorts             := Seq(),
      applicationJavaOpts          := Seq(),
      docker                      <<= (docker dependsOn assembly),
      imageNames in docker := {
        val base = ImageName(
          registry = None,
          namespace = Some(organization.value),
          repository = projectName.value,
          tag = Some(version.value)
        )
        Seq(base, base.copy(tag = None))
      },
      dockerfile in docker := {
        val jarFile = (assemblyOutputPath in assembly).value
        val mainclass = (mainClass in assembly).value.getOrElse(sys.error("assembly:mainClass must be set"))
        val appPath = "/app"
        val jarTarget = s"$appPath/${jarFile.name}"
        val confTarget = s"$appPath/conf"
        val dataTarget = s"$appPath/data"
        val jvmHeap = applicationJvmHeap.?.value.getOrElse("1g")
        val javaOpts = applicationJavaOpts.value
        val exposed = applicationPorts.value
        new Dockerfile {
          from("martinseeler/oracle-server-jre:1.8_66")
          env("JVM_HEAP", jvmHeap)
          env("JAVA_OPTS", javaOpts.mkString(" "))
          env("JVM_ARGS", "")
          add(jarFile, jarTarget)
          run("mkdir", confTarget)
          volume(confTarget, dataTarget)
          workDir(appPath)
          expose(exposed: _*)
          entryPointShell("exec", "java", "-cp", s"$confTarget:$jarTarget", "$JAVA_OPTS", "-Xms${JVM_HEAP}", "-Xmx${JVM_HEAP}", "$JVM_ARGS", mainclass)
        }
      }
    ) ++ additional
  }
  import KCode._, KReleaseSteps._, KTut._
  import autoImport.{Github => _, Developer => _, ScalaMainVersion => _, JavaVersion => _, _}

  override lazy val projectSettings =
    pluginSettings ++ orgaSettings ++ compilerSettings ++
    docsSettings ++ releaseSettings ++ miscSettings

  override lazy val buildSettings =
    SbtGit.versionWithGit

  override lazy val projectConfigurations =
    List(RunDebug)

  lazy val orgaSettings = Seq(
                    name := s"${projectName.value}-${thisProject.value.id}",
              maintainer := organizationName.value,
              githubDevs := githubProject.?.value.map(gh ⇒ Developer(gh.org, maintainer.value)).toSeq,
    organizationHomepage := githubProject.?.value.map(_.organization),
                homepage := githubProject.?.value.map(_.repository)
  )

  lazy val compilerSettings = Seq(
                       scalaMainVersion := ScalaMainVersion(scalaBinaryVersion.value),
                            javaVersion := JavaVersion.Java18,
               experimentalJava8Support := false,
    scalacOptions in Compile            := KScalaFlags(scalaMainVersion.value, javaVersion.value, experimentalJava8Support.value),
    scalacOptions in    Test           ++= Seq("-Xcheckinit", "-Yrangepos"),
    scalacOptions in (Compile, console) ~= (_ filterNot (x => x == "-Xfatal-warnings" || x.startsWith("-Ywarn"))),
    scalacOptions in    (Test, console) ~= (_ filterNot (x => x == "-Xfatal-warnings" || x.startsWith("-Ywarn")))
  )

  lazy val docsSettings = Seq(
     autoAPIMappings := true,
    latestVersionTag := GitKeys.gitReader.value.withGit(g ⇒ findLatestVersion(g.asInstanceOf[JGit])),
       latestVersion := latestVersionTag.value.getOrElse(version.value),
          genModules := generateModules(state.value, sourceManaged.value, streams.value.cacheDirectory, thisProject.value.dependencies),
          makeReadme := mkReadme(state.value, buildReadmeContent.?.value.getOrElse(Nil), readmeFile.?.value, readmeFile.?.value),
        commitReadme := addAndCommitReadme(state.value, makeReadme.value, readmeCommitMessage.?.value, releaseVcs.value),
            pomExtra := pomExtra.value ++ makePomExtra(sbtVersion.value, githubDevs.value, githubProject.?.value)
  )

  lazy val releaseSettings = Seq(
                    scmInfo := githubProject.?.value.map(_.scmInfo),
                   licenses := List("Apache 2" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
                 developers := sbtDevelopers(sbtVersion.value, githubDevs.value),
             pomPostProcess := { (node) => removeScoverage.transform(node).head },
          previousArtifacts := latestVersionTag.value.map(v ⇒ organization.value %% name.value % v).filter(_ ⇒ publishArtifact.value).toSet,
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
      pushGithubPages,
      commitTheReadme,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

  lazy val miscSettings = Seq(
        shellPrompt := { state => configurePrompt(state) },
        logBuffered := false,
      updateOptions ~= (_.withCachedResolution(cachedResoluton = true)),
    cleanKeepFiles ++= Seq("resolution-cache", "streams").map(target.value / _)
  )

  lazy val pluginSettings =
    mimaDefaultSettings ++
    inConfig(Compile)(headerSettings) ++
    inConfig(Test)(headerSettings)

  lazy val headerSettings = Seq(
    compile := compile.dependsOn(createHeaders).value,
    headers <<= (startYear, maintainer) apply headerConfig
  )
}

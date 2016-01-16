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

import sbt.Keys._
import sbt._
import sbtrelease.Vcs

import scala.annotation.tailrec
import scala.collection.immutable.ListMap


object KTut {

  private[this] val tutLine = raw"tut: (\d+)".r
  private[this] val titleLine = raw"title: (.+)".r
  private[this] val directLink = raw".*?\[&(?:l|r)aquo;.*?\]\([^/]+.html\).*".r
  private[this] val internalLink = raw".*\(([^/]+.html)\).*".r
  private[this] val slugify = (title: String) ⇒ title.replaceAll(raw"\s+", "-").toLowerCase(java.util.Locale.ENGLISH)
  private[this] val withoutExtension = (name: String) ⇒ name.substring(0, name.lastIndexOf('.'))


  def generateModules(state: State, dir: File, cacheDir: File, modules: Seq[ClasspathDep[ProjectRef]]): Seq[(File, String)] = {
    val files = new GenerateModulesTask(state, dir, cacheDir, modules.map(_.project)).apply()
    files.map(x ⇒ (x, x.getName))
  }


  private def parseTutFiles(srcs: Seq[(File,String)]): Seq[TutFile] = {
    val sources = srcs.map(_._1)
    sources.flatMap(parseFrontMatter).sortBy(_.index)
  }

  private def getTitles(tuts: Seq[TutFile]): Map[String, String] =
    ListMap(tuts.map(f ⇒ withoutExtension(f.file.getName) → f.title): _*)

  private def parseFrontMatter(file: File): Option[TutFile] = {
    val lines = IO.readLines(file)
    val (front, content) = lines.dropWhile(_ == "---").span(_ != "---")
    for {
      index ← front.collectFirst {case tutLine(idx) ⇒ idx.toInt}
      title ← front.collectFirst {case titleLine(t) ⇒ t}
    } yield TutFile(title, file, content, index)
  }

  private def parseTutContent(latest: String, titles: Map[String, String])(tut: TutFile): List[String] = {
    import tut._
    val actualContent = content.drop(1).withFilter {
      case directLink() ⇒ false
      case _            ⇒ true
    }.map(replaceLinks(latest, titles))
    "" :: "## " + title :: "" :: actualContent
  }

  @tailrec
  private def replaceLinks(version: String, titles: Map[String, String])(line: String): String = line match {
    case line@internalLink(link) ⇒
      val newLink = titles.get(withoutExtension(link)).fold(link)(l ⇒ s"#${slugify(l)}")
      replaceLinks(version, titles)(line.replaceAllLiterally(link, newLink))
    case _                       ⇒ line.replaceAll(raw"\{\{ site\.data\.version\.version \}\}", version)
  }

  def mkReadme(state: State, srcs: Seq[(File,String)], tpl: Option[File], out: Option[File]): Option[File] = {
    tpl.filter(_.exists()).flatMap { template ⇒
      out.flatMap {outputFile ⇒
        val extracted = Project.extract(state)
        val latest = extracted.get(KSbtKeys.latestVersion)
        val tuts = parseTutFiles(srcs)
        val titles = getTitles(tuts)
        val lines = tuts.flatMap(parseTutContent(latest, titles))
        Some(lines).filter(_.nonEmpty).map { ls ⇒
          val targetLines = IO.readLines(template)
          val (head, middle) = targetLines.span(_ != "<!--- TUT:START -->")
          val (_, tail) = middle.span(_ != "<!--- TUT:END -->")
          IO.writeLines(outputFile, head)
          IO.writeLines(outputFile, middle.take(1), append = true)
          IO.writeLines(outputFile, makeLibraryDeps(extracted, latest), append = true)
          IO.writeLines(outputFile, Seq.fill(2)(""), append = true)
          IO.writeLines(outputFile, makeToc(titles), append = true)
          IO.writeLines(outputFile, ls, append = true)
          IO.writeLines(outputFile, tail, append = true)
          outputFile
        }
      }
    }
  }

  private def makeLibraryDeps(extracted: Extracted, version: String): Seq[String] = {
    val modules = extracted.get(thisProject).dependencies.map(_.project)
    val dependencies = modules.flatMap { proj ⇒
      val org = extracted.get(organization in proj)
      val module = extracted.get(name in proj)
      List(s"""  "$org" %% "$module" % "$version"""", ",")
    }.init
    val deps = dependencies.grouped(2).map(_.mkString("")).toList
    "```scala" :: "libraryDependencies ++= List(" :: deps ::: ")" :: "```" :: Nil
  }

  private def makeToc(titles: Map[String, String]): List[String] =
    List("## [Documentation][docs]", "") ++ titles.values.map { title ⇒
      s"- [$title](#${slugify(title)})"
    }

  def addAndCommitReadme(state: State, readme: Option[File], message: Option[String], maybeVcs: Option[Vcs]): Option[File] = for {
    vcs ← maybeVcs
    file ← readme
    msg ← message
    relative ← IO.relativize(vcs.baseDir, file)
    ff ← tryCommit(msg, vcs, file, relative, state.log)
  } yield ff

  private def tryCommit(message: String, vcs: Vcs, file: File, relative: String, log: Logger): Option[File] = {
    vcs.add(relative) !! log
    val status = vcs.status.!!.trim
    if (status.nonEmpty) {
      vcs.commit(message) ! log
      Some(file)
    } else {
      None
    }
  }

  private class GenerateModulesTask(state: State, dir: File, cacheDir: File, modules: Seq[ProjectRef]) {
    val tempModulesFile = cacheDir / "gen-modules" / "modules.yml"
    val tempVersionFile = cacheDir / "gen-modules" / "version.yml"
    val modulesFile = dir / "modules.yml"
    val versionFile = dir / "version.yml"

    def apply(): Seq[File] = {
      mkFiles()
      List(
        cachedCopyOf(tempVersionFile, versionFile),
        cachedCopyOf(tempModulesFile, modulesFile)
      )
    }

    def mkFiles() = {
      val extracted = Project.extract(state)
      val latest = extracted.get(KSbtKeys.latestVersion)
      val lines = mkLines(extracted, latest)
      IO.writeLines(tempModulesFile, lines)
      IO.writeLines(tempVersionFile, s"version: $latest" :: Nil)
    }

    def cachedCopyOf(from: File, to: File): File = {
      val cacheFile = cacheDir / "gen-modules" / "cached-inputs" / from.getName
      val check = Tracked.inputChanged(cacheFile) {(hasChanged, input: HashFileInfo) ⇒
        if (hasChanged || !to.exists()) {
          IO.copyFile(from, to, preserveLastModified = true)
        }
      }
      check(FileInfo.hash(from))
      to
    }

    def mkLines(extracted: Extracted, latestVersion: String) =
      modules.flatMap { proj ⇒
        Seq(
          s"- organization: ${extracted.get(organization in proj)}",
          s"  name: ${extracted.get(name in proj)}",
          s"  version: $latestVersion"
        )
      }
  }

  private case class TutFile(title: String, file: File, content: List[String], index: Int)
}

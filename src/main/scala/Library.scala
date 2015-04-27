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

import scala.language.{implicitConversions, dynamics}

case class ModuleDef(artefact: String, config: String, version: Option[String])

sealed trait Library {
  def libraries: Seq[ModuleID]

  final def and(other: Library): Library =
    new Library.CombinedLibraries(this, other)

  final def &&(other: Library): Library =
    and(other)
}

sealed trait ApplyMagnet {
  def version: Option[String]
  def config: String
}
object ApplyMagnet {
  implicit def applyFromVersion(v: String): ApplyMagnet = new ApplyMagnet {
    def config: String = Compile.name
    def version: Option[String] = Some(v) filterNot (_.isEmpty)
  }
  implicit def applyFromConfiguration(c: Configuration): ApplyMagnet = new ApplyMagnet {
    def config: String = c.name
    def version: Option[String] = None
  }
}

class ModuleConfig(
    group: String,
    artefactPrefix: String,
    latestVersion: String,
    cross: Boolean = true,
    private val modules: Seq[ModuleDef] = Vector.empty)
  extends Dynamic with Library {

  final def libraries: Seq[ModuleID] =
    latest.modules.collect {
      case ModuleDef(a, c, Some(v)) if !v.isEmpty ⇒
        if (cross) group %% a % v % c
        else       group  % a % v % c
    }

  final def at(v: String): ModuleConfig = {
    val version = Some(v) filterNot (_.isEmpty)
    modModules(m ⇒ m.copy(version = m.version orElse version))
  }

  final def latest: ModuleConfig =
    at(latestVersion)

  // Akka.core
  def selectDynamic(name: String): ModuleConfig =
    addModule(ModuleDef(artefactPrefix + name, Compile.name, None))

  // Akka.core("2.3.1"), Akka.core(Test)
  def applyDynamic(name: String)(cv: ApplyMagnet, cvs: ApplyMagnet*): ModuleConfig =
    (cv +: cvs).map { x ⇒
      ModuleDef(artefactPrefix + name, x.config, x.version)
    }.foldLeft(this)(_.addModule(_))

  // Akka.core(test="2.3.9")
  def applyDynamicNamed(name: String)(config: (String, String), configs: (String, String)*): ModuleConfig =
    (config +: configs).map { case (c, v) ⇒
      val config = Some(c) filterNot (_.isEmpty) getOrElse Compile.name
      val version = Some(v) filterNot (_.isEmpty)
      ModuleDef(artefactPrefix + name, config, version)
    }.foldLeft(this)(_.addModule(_))

  private def addModule(m: ModuleDef): ModuleConfig =
    new ModuleConfig(group, artefactPrefix, latestVersion, cross, modules :+ m)

  private def modModules(f: ModuleDef ⇒ ModuleDef): ModuleConfig =
    new ModuleConfig(group, artefactPrefix, latestVersion, cross, modules.map(f))
}

object Library {

  object Akka extends ModuleConfig("com.typesafe.akka", "akka-", "2.3.9")
  object Lucene extends ModuleConfig("org.apache.lucene", "lucene-", "5.1.0", cross = false)
  object Netty extends ModuleConfig("io.netty", "netty-", "4.0.27", cross = false)
  object Rx extends ModuleConfig("io.netty", "", "", cross = false)
  object Scalaz extends ModuleConfig("org.scalaz", "scalaz-", "7.1.1")
  object Shapeless extends ModuleConfig("com.chuusai", "shapeless-", "2.1.0",
    modules = Vector(ModuleDef("shapeless", Compile.name, None)))
  object Specs2 extends ModuleConfig("org.specs2", "specs2-", "3.5")


  private class CombinedLibraries(left: Library, right: Library) extends Library {
    final def libraries: Seq[ModuleID] =
      left.libraries ++ right.libraries
  }
}

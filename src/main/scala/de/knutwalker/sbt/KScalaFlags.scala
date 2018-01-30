/*
 * Copyright 2015 – 2017 Paul Horn
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

import de.knutwalker.sbt.JavaVersion.{ Java16, Java18, Java17, Java15, Java19 }
import de.knutwalker.sbt.ScalaMainVersion.{ Scala212, Scala211, Scala210, Unknown }

object KScalaFlags {

  def apply(scala: ScalaMainVersion, java: JavaVersion, experimental: Boolean): Seq[String] =
    scalacFlags(scala, experimental) :+ javaTargetOpt(scala, java)

  private def scalacFlags(scala: ScalaMainVersion, experimental: Boolean) = {
    val specificFlags = scala match {
      case Scala211 ⇒ flagsFor11
      case Scala212 ⇒ flagsFor12
      case _ ⇒ flagsFor10
    }
    val otherFlags = if (experimental) experimentalFlags else Seq()
    universalFlags ++ specificFlags ++ otherFlags
  }

  private def flagsFor10 = Seq(
    "-Xlint",
    "-Yclosure-elim",
    "-Ydead-code"
  )

  private def flagsFor11 = Seq(
    "-Xlint:_",
    "-Yconst-opt",
    "-Ywarn-infer-any",
    "-Yclosure-elim",
    "-Ydead-code"
    // unused reports are too aggressive, imho
    // "-Ywarn-unused",
    // "-Ywarn-unused-import"
  )

  private def flagsFor12 = Seq(
    "-Xlint:_",
    "-Ywarn-infer-any",
    "-opt-inline-from:<sources>"
  )

  private def universalFlags = Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-explaintypes",
    "-feature",
    "-language:_",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xfuture",
    "-Yno-adapted-args",
    "-Ywarn-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-inaccessible",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )

  private def experimentalFlags = Seq(
     "-Xexperimental", // CHECK: could break and slow down stuff
     "-Ybackend:GenBCode", // CHECK: wait for 2.11.8 due to sbt/sbt#2076 and scala/scala#4588
     "-Ydelambdafy:method", // CHECK: wait for 2.11.8 due to sbt/sbt#2076 and scala/scala#4588
     "-Yopt:l:classpath" // CHECK: wait for 2.11.8 due to sbt/sbt#2076 and scala/scala#4588
  )

  private def javaTargetOpt(scala: ScalaMainVersion, java: JavaVersion) =
    s"-target:jvm-${targetJvmVersion(scala, java).jvm}"

  private def targetJvmVersion(scala: ScalaMainVersion, java: JavaVersion): JavaVersion =
    java match {
      case Java19 ⇒ scala match {
        case Unknown  ⇒ Java15
        case Scala210 ⇒ Java17
        case _        ⇒ Java18
      }
      case Java18 ⇒ scala match {
        case Unknown  ⇒ Java15
        case Scala210 ⇒ Java17
        case _        ⇒ Java18
      }
      case Java17 ⇒ scala match {
        case Unknown ⇒ Java15
        case _       ⇒ Java17
      }
      case Java16 ⇒ scala match {
        case Unknown ⇒ Java15
        case _       ⇒ Java16
      }
      case _      ⇒ Java15
    }
}

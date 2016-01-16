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

sealed abstract class ScalaMainVersion(val cross: String)
object ScalaMainVersion {
  case object Scala210 extends ScalaMainVersion("2.10")
  case object Scala211 extends ScalaMainVersion("2.11")
  case object Scala212 extends ScalaMainVersion("2.12")
  case object Unknown extends ScalaMainVersion("")

  def apply(cross: String): ScalaMainVersion = cross match {
    case "2.12" ⇒ Scala212
    case "2.11" ⇒ Scala211
    case "2.10" ⇒ Scala210
    case _      ⇒ Unknown
  }

  def unapply(s: ScalaMainVersion): Option[String] =
    if (s == Unknown) None
    else Some(s.cross)
}

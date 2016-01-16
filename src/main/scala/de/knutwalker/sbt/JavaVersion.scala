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

sealed abstract class JavaVersion(val jvm: String)
object JavaVersion {
  case object Java15 extends JavaVersion("1.5")
  case object Java16 extends JavaVersion("1.6")
  case object Java17 extends JavaVersion("1.7")
  case object Java18 extends JavaVersion("1.8")
  case object Java19 extends JavaVersion("1.9")
  case object Unknown extends JavaVersion("")

  def apply(v: String): JavaVersion = v match {
    case "1.5" ⇒ Java15
    case "1.6" ⇒ Java16
    case "1.7" ⇒ Java17
    case "1.8" ⇒ Java18
    case "1.9" ⇒ Java19
    case _     ⇒ Unknown
  }

  def unapply(j: JavaVersion): Option[String] =
    if (j == Unknown) None
    else Some(j.jvm)
}

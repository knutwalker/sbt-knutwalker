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

import sbtrelease.Version

object VersionOrdering extends VersionOrdering
trait VersionOrdering {
  implicit val versionOrdering = new Ordering[Version] {
    @scala.annotation.tailrec
    private def compareSubversions(vs: List[(Int, Int)]): Int = vs match {
      case (left, right) :: rest ⇒ left compare right match {
        case 0 ⇒ compareSubversions(rest)
        case a ⇒ a
      }
      case Nil                   ⇒ 0
    }
    def compare(x: Version, y: Version): Int =
      x.major compare y.major match {
        case 0 ⇒ compareSubversions(x.subversions.zip(y.subversions)(collection.breakOut)) match {
          case 0 ⇒ (x.qualifier, y.qualifier) match {
            case (None, None)       ⇒ 0
            case (Some(_), Some(_)) ⇒ 0
            case (None, _)          ⇒ 1
            case (_, None)          ⇒ -1
          }
          case a ⇒ a
        }
        case a ⇒ a
      }
  }
}

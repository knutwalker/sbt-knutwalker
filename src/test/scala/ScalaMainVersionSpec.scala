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

import de.knutwalker.sbt.ScalaMainVersion.{Scala210, Scala211, Scala212, Unknown}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object ScalaMainVersionSpec extends Specification with ScalaCheck {

  "ScalaMainVersion" >> {

    "apply" in {
      ScalaMainVersion("2.10") ==== Scala210
      ScalaMainVersion("2.11") ==== Scala211
      ScalaMainVersion("2.12") ==== Scala212
    }

    "apply unknonw" in prop { (s: String) ⇒
      (!s.matches("2\\.(10|11|12)")) ==> {
        ScalaMainVersion(s) ==== Unknown
      }
    }

    "unapply" in {
      Scala210 match {
        case ScalaMainVersion(s) ⇒ s ==== "2.10"
      }
      Scala211 match {
        case ScalaMainVersion(s) ⇒ s ==== "2.11"
      }
      Scala212 match {
        case ScalaMainVersion(s) ⇒ s ==== "2.12"
      }
      Unknown match {
        case ScalaMainVersion(s) ⇒ failure("Unknown should not match as a ScalaMainVersion")
        case x                   ⇒ success("Unknown did not match a ScalaMainVersion")
      }
    }
  }

}

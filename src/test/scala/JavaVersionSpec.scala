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

import de.knutwalker.sbt.JavaVersion.{Unknown, Java19, Java18, Java16, Java17, Java15}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object JavaVersionSpec extends Specification with ScalaCheck {

  "JavaVersion" >> {

    "apply" in {
      JavaVersion("1.5") ==== Java15
      JavaVersion("1.6") ==== Java16
      JavaVersion("1.7") ==== Java17
      JavaVersion("1.8") ==== Java18
      JavaVersion("1.9") ==== Java19
    }

    "apply unknonw" in prop { (s: String) ⇒
      (!s.matches("1\\.[5-9]")) ==> {
        JavaVersion(s) ==== Unknown
      }
    }

    "unapply" in {
      Java15 match {
        case JavaVersion(j) ⇒ j ==== "1.5"
      }
      Java16 match {
        case JavaVersion(j) ⇒ j ==== "1.6"
      }
      Java17 match {
        case JavaVersion(j) ⇒ j ==== "1.7"
      }
      Java18 match {
        case JavaVersion(j) ⇒ j ==== "1.8"
      }
      Java19 match {
        case JavaVersion(j) ⇒ j ==== "1.9"
      }
      Unknown match {
        case JavaVersion(j) ⇒ failure("Unknown should not match as a JavaVersion")
        case x              ⇒ success("Unknown did not match a JavaVersion")
      }
    }
  }

}

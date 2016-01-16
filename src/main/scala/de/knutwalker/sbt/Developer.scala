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
import sbt.URL

case class Developer(id: String, name: String, identity: Developer.Identity = Developer.Github) {

  def url: URL =
    sbt.url(identity.url(id))

  def sbtDev: sbt.Developer =
    sbt.Developer(id, name, "", url)
}

object Developer {
  def apply(id: String): Developer =
    new Developer(id, id)


  sealed trait Identity { def url(id: String): String }

  case object Github extends Identity {
    def url(id: String) = s"https://github.com/$id/"
  }

  case object Twitter extends Identity {
    def url(id: String) = s"https://twitter.com/$id/"
  }
}

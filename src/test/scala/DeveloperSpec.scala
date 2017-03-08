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

import de.knutwalker.sbt.Developer.{Twitter, Identity}
import org.scalacheck.{Gen, Arbitrary}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import java.net.URL

object DeveloperSpec extends Specification with ScalaCheck {

  "Developer" >> {

    "name defaults to id" in prop { (id: String) ⇒
      Developer(id) ==== Developer(id, id)
    }

    "url defaults to github" in prop { (id: String) ⇒
      Developer(id).url ==== new URL("https", "github.com", s"/$id/")
    }

    "works also for twitter" in prop { (id: String) ⇒
      Developer(id, id, identity = Twitter).url ==== new URL("https", "twitter.com", s"/$id/")
    }

    "converts to sbt dev" in prop { (id: String, name: String, identity: Identity) ⇒
      val host = identity match {
        case Developer.Github ⇒ "github.com"
        case Developer.Twitter ⇒ "twitter.com"
      }
      val url = new URL("https", host, s"/$id/")
      Developer(id, name, identity).sbtDev ==== sbt.Developer(id, name, "", url)
    }
  }

  implicit val arbitraryIdentity: Arbitrary[Identity] =
    Arbitrary(Gen.oneOf(Developer.Github, Twitter))
}

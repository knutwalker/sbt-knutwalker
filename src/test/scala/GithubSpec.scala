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

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import sbt.ScmInfo

import java.net.URL

object GithubSpec extends Specification with ScalaCheck {

  "Github" >> {

    "user url" in prop { (user: String) ⇒
      Github(user, "").organization ==== new URL("https", "github.com", s"/$user/")
    }

    "rep uorl" in prop { (user: String, repo: String) ⇒
      Github(user, repo).repository ==== new URL("https", "github.com", s"/$user/$repo/")
    }

    "produces a pom snippet" in prop { (user: String, repo: String) ⇒
      val expected = ScmInfo(
        new URL("https", "github.com", s"/$user/$repo/"),
        "scm:git:" + new URL("https", "github.com", s"/$user/$repo.git"),
        Some(s"scm:git:ssh://git@github.com:$user/$repo.git")
      )
      Github(user, repo).scmInfo ==== expected
    }
  }
}

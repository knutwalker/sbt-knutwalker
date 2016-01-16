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

import sbt._

case class Github(org: String, repo: String) {

  def repository: URL =
    url(s"https://github.com/$org/$repo/")

  def organization: URL =
    url(s"https://github.com/$org/")

  def remoteSsh: String =
    s"git@github.com:$org/$repo.git"

  def remoteHttp: String =
    s"github.com/$org/$repo.git"

  def scmInfo: ScmInfo = ScmInfo(
    repository,
    s"scm:git:https://$remoteHttp",
    Some(s"scm:git:ssh://$remoteSsh")
  )
}

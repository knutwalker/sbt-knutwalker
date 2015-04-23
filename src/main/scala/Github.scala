package de.knutwalker.sbt

import sbt._

case class Github(org: String, repo: String) {

  def user =
    url(s"https://github.com/$org/$repo/")

  def organization =
    url(s"https://github.com/$org/")

  def pomExtra =
    <scm>
      <connection>scm:git:https://github.com/{org}/{repo}.git</connection>
      <developerConnection>scm:git:ssh://git@github.com:{org}/{repo}.git</developerConnection>
      <url>https://github.com/{org}/{repo}</url>
    </scm>

}

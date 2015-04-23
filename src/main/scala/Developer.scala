package de.knutwalker.sbt

case class Developer(id: String, name: String, identity: Developer.Identity = Developer.Github) {

  def pomExtra =
    <developer>
      <id>{id}</id>
      <name>{name}</name>
      <url>{identity.url(id)}</url>
    </developer>

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

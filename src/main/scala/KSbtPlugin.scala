package de.knutwalker.sbt

import sbt._
import sbt.Keys._
import scala.xml.{Node => XNode, NodeSeq => XNodeSeq}
import scala.xml.transform.{RewriteRule, RuleTransformer}
import de.heikoseeberger.sbtheader.{HeaderPlugin, AutomateHeaderPlugin}
import de.heikoseeberger.sbtheader.license.Apache2_0
import com.typesafe.sbt.pgp.PgpKeys.publishSigned
import org.scoverage.coveralls.CoverallsPlugin
import scoverage.ScoverageSbtPlugin
import sbtrelease.ReleasePlugin._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.ReleaseStep
import xerial.sbt.Sonatype._
import xerial.sbt.Sonatype.SonatypeKeys.sonatypeReleaseAll


// TODO:
//  - versions like scalazVersion
//    - adding resolvers and libraryDependencies
//    - confirable with module lists (e.g. "core", "effect", ...)
//  - make releasesteps configurable
//  - make scalac options configurable
object KSbtPlugin extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = plugins.JvmPlugin && HeaderPlugin && ScoverageSbtPlugin && CoverallsPlugin

  object autoImport extends KSbtKeys {
    lazy val dontRelease: Seq[Def.Setting[_]] = List(
              publish := (),
         publishLocal := (),
      publishArtifact := false
    )
  }

  import autoImport._

  override lazy val projectSettings =
   ksbtSettings ++
   inConfig(Compile)(headerSettings) ++
   inConfig(Test)(headerSettings)

  lazy val ksbtSettings: Seq[Def.Setting[_]] = List(
     maintainer := organizationName.value,
     githubDevs := Seq.empty,
    releaseThis := true
  ) ++ derivedSettings ++ compilerSettings ++ publishSettings

  lazy val derivedSettings: Seq[Def.Setting[_]] = List(
    organizationHomepage := githubProject.?.value.map(_.organization),
                homepage := githubProject.?.value.map(_.user),
             shellPrompt := { state => configurePrompt(state) },
             logBuffered := false,
                licenses := List("Apache 2" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
          pomPostProcess := { (node) => rewriteTransformer.transform(node).head },
                pomExtra := pomExtra.value ++ extraPom(githubProject.?.value, githubDevs.value),
   libraryDependencies <++= (scalaBinaryVersion, akkaVersion.?, luceneVersion.?, nettyVersion.?, rxJavaVersion.?, rxScalaVersion.?, shapelessVersion.?, scalazVersion.?) apply addLibrary,
             resolvers <++= (scalazVersion.?) apply addResolvers,
         cleanKeepFiles ++= List("resolution-cache", "streams").map(target.value / _),
           updateOptions ~= (_.withCachedResolution(true))
  )

  lazy val compilerSettings: Seq[Def.Setting[_]] = List(
               scalacOptions in Compile := scalacCompileOptions(scalaBinaryVersion.value),
    scalacOptions in (Compile, console) ~= (_ filterNot (x => x == "-Xfatal-warnings" || x.startsWith("-Ywarn"))),
       scalacOptions in (Test, console) ~= (_ filterNot (x => x == "-Xfatal-warnings" || x.startsWith("-Ywarn"))),
                  scalacOptions in Test += "-Yrangepos"
  )

  lazy val headerSettings: Seq[Def.Setting[_]] = {
    import HeaderPlugin.autoImport.headers
    List(
      compile := compile.dependsOn(HeaderPlugin.autoImport.createHeaders).value,
      headers <<= (startYear, maintainer) apply headerConfig
    )
  }

  lazy val publishSettings: Seq[Def.Setting[_]] =
    releaseSettings ++ sonatypeSettings ++ List(
              //        publish <<= releaseThis map { r => if (r) publish.value else () },
              //   publishLocal <<= releaseThis map { r => if (r) publishLocal.value else () },
              // publishArtifact := releaseThis.value,
      publishArtifact in Test := false,
                   tagComment := s"Release version ${version.value}",
                commitMessage := s"Set version to ${version.value}",
                  versionBump := sbtrelease.Version.Bump.Bugfix,
               releaseProcess := List[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runClean,
        runTest,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        publishSignedArtifacts,
        releaseToCentral,
        setNextVersion,
        commitNextVersion,
        pushChanges,
        publishArtifacts
      )
    )

  private def headerConfig(year: Option[Int], maintainer: String) = {
    val thisYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = List(year.getOrElse(thisYear), thisYear).distinct.mkString(" – ")
    Map("java"  -> Apache2_0(years, maintainer),
        "scala" -> Apache2_0(years, maintainer))
  }

  private def scalacCompileOptions(cross: String) = {
    val crossOpts = cross match {
      case "2.11" => List(
        "-Xlint:_",
        "-Yconst-opt",
        "-Ywarn-infer-any",
        "-Ywarn-unused",
        "-Ywarn-unused-import")
      case _      => List(
        "-Xlint")
    }
    crossOpts ++ List(
      "-deprecation",
      "-encoding",  "UTF-8",
      "-feature",
      "-language:_",
      // "-optimise",
      "-unchecked",
      "-target:jvm-1.7",
      "-Xcheckinit",
      "-Xfatal-warnings",
      "-Xfuture",
      "-Yclosure-elim",
      "-Ydead-code",
      // "-Yinline",
      "-Yno-adapted-args",
      // "-Yinline-handlers",
      // "-Yinline-warnings",
      "-Ywarn-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-inaccessible",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-Ywarn-numeric-widen")
  }

  private def addLibrary(cross: String,
      akka: Option[String],
      lucene: Option[String],
      netty: Option[String],
      rxJava: Option[String],
      rxScala: Option[String],
      shapeless: Option[String],
      scalaz: Option[String]) = {

    akka.toList.flatMap(Dependencies.akka) ++
    lucene.toList.flatMap(Dependencies.lucene) ++
    netty.toList.flatMap(Dependencies.netty) ++
    rxJava.toList.flatMap(Dependencies.rxJava) ++
    rxScala.toList.flatMap(Dependencies.rxScala) ++
    shapeless.toList.flatMap(Dependencies.shapeless) ++
    scalaz.toList.flatMap(Dependencies.scalaz)
  }

  private def addResolvers(scalaz: Option[String]) =
    scalaz match {
      case Some(_) => List("Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases")
      case None    => Nil
    }

  private def configurePrompt(st: State) = {
    import scala.Console._
    val name = Project.extract(st).currentRef.project
    val color = GREEN
    (if (name == "parent") "" else s"[$color$name$RESET] ") + "> "
  }

  private def extraPom(g: Option[Github], devs: Seq[Developer]) = {
    val developerInfo = if (devs.nonEmpty)
      <developers>{devs.map(_.pomExtra)}</developers>
    else
      XNodeSeq.Empty
    val scmInfo = g.fold(XNodeSeq.Empty)(_.pomExtra)
    scmInfo ++ developerInfo
  }

  private val rewriteRule = new RewriteRule {
    override def transform(n: XNode): XNodeSeq =
      if (n.label == "dependency" && (n \ "scope").text == "provided" && (n \ "groupId").text == "org.scoverage")
        XNodeSeq.Empty
      else n
  }

  private val rewriteTransformer = new RuleTransformer(rewriteRule)

  private lazy val publishSignedArtifacts = publishArtifacts.copy(
    action = { state =>
      val extracted = Project extract state
      val ref = extracted get thisProjectRef
      extracted.runAggregated(publishSigned in Global in ref, state)
    },
    enableCrossBuild = true
  )

  private lazy val releaseToCentral = ReleaseStep(
    action = { state =>
      val extracted = Project extract state
      val ref = extracted get thisProjectRef
      extracted.runAggregated(sonatypeReleaseAll in Global in ref, state)
    },
    enableCrossBuild = true
  )

}

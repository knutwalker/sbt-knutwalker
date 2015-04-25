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

import de.knutwalker.sbt.JavaVersion.{Java15, Java16, Java17, Java18, Java19}
import de.knutwalker.sbt.ScalaMainVersion.{Scala210, Scala211, Unknown}


trait ScalacOptions {

  def flags(main: ScalaMainVersion): Seq[String]

  def flattened: Seq[ScalacOptions] =
    Vector(this)

  def requires: Seq[ScalacOptions] =
    Nil

  def conflictsWith: Seq[ScalacOptions] =
    Nil

  final def finalFlags(main: ScalaMainVersion): Seq[String] =
    finalOpts.flatMap(_.flags(main))

  final def finalOpts: Seq[ScalacOptions] = {
    val flat = flattened
    val required = flat.flatMap(_.requires)
    val withReqs = (flat ++ required).distinct
    val conflicts = withReqs.flatMap(_.conflictsWith).toSet
    withReqs filterNot conflicts
  }

  final def and(other: ScalacOptions): ScalacOptions =
    ScalacOptions.AddOptions(this, other)

  final def but(other: ScalacOptions): ScalacOptions =
    ScalacOptions.RemoveOptions(this, other)

  final def not(other: ScalacOptions): ScalacOptions =
    but(other)

  final def +(other: ScalacOptions): ScalacOptions =
    and(other)

  final def ++(other: ScalacOptions): ScalacOptions =
    and(other)

  final def -(other: ScalacOptions): ScalacOptions =
    but(other)

  final def --(other: ScalacOptions): ScalacOptions =
    but(other)
}

object ScalacOptions {

  case object Lint extends ScalacOptions {
    def flags(main: ScalaMainVersion): Seq[String] = main match {
      case Scala211 ⇒ List("-Xlint:_")
      case _        ⇒ List("-Xlint")
    }
  }

  case object GoodMeasure extends ScalacOptions {
    def flags(main: ScalaMainVersion): Seq[String] = {
      val always = List(
        "-Xcheckinit", "-Xfuture", "-Yclosure-elim",
        "-Ywarn-inaccessible", "-Ywarn-nullary-override", "-Ywarn-nullary-unit"
      )
      val byVersion = main match {
        case Scala210 | Unknown ⇒ List()
        case _                  ⇒ List("-Yconst-opt")
      }
      always ++ byVersion
    }
  }

  case object DisallowUnunsedCode extends ScalacOptions with RequireFatal {
    def flags(main: ScalaMainVersion): Seq[String] = main match {
      case Scala210 | Unknown ⇒ List()
      case _                  ⇒ List("-Ywarn-unused", "-Ywarn-unused-import")
    }
  }

  case object DisallowInferredAny extends ScalacOptions with RequireFatal {
    def flags(main: ScalaMainVersion): Seq[String] = main match {
      case Scala210 | Unknown ⇒ List()
      case _                  ⇒ List("-Ywarn-infer-any")
    }
  }

  case object Inline extends General("-Yinline", "-Yinline-handlers") {
    override def requires: Seq[ScalacOptions] = Optimise :: Nil

    // Inlining and fatal warning do very likely lead to code that cannot compile. See SI-8140
    override def conflictsWith: Seq[ScalacOptions] = FatalWarnings :: Nil
  }

  case object InlineWarnings extends General("-Yinline-warnings") {
    override def requires: Seq[ScalacOptions] = Inline :: Nil
  }

  case object SimpleWarnings extends General("-deprecation", "-feature", "-unchecked")
  case object ExplainTypes extends General("-explaintypes")
  case object Optimise extends General("-optimise")

  case object Experimental extends General("-Xexperimental")
  case object FatalWarnings extends General("-Xfatal-warnings")
  case object DisablePredef extends General("-Yno-predef")

  case object EliminateDeadCode extends General("-Ydead-code", "-Ywarn-dead-code") with RequireFatal
  case object DisallowNumericWidening extends General("-Ywarn-numeric-widen") with RequireFatal
  case object DisallowValueDiscard extends General("-Ywarn-value-discard") with RequireFatal
  case object DisallowAdaptedArgs extends General("-Yno-adapted-args", "-Ywarn-adapted-args") with RequireFatal

  case object Utf8 extends General("-encoding", "UTF-8")

  sealed abstract class LanguageFeature(feature: String) extends ScalacOptions {
    final def flags(main: ScalaMainVersion): Seq[String] =
      Vector(s"-language:$feature")
  }

  object LanguageFeature {
    case object Dynamics extends LanguageFeature("dynamics")
    case object PostfixOps extends LanguageFeature("postfixOps")
    case object ReflectiveCalls extends LanguageFeature("reflectiveCalls")
    case object ImplicitConversions extends LanguageFeature("implicitConversions")
    case object HigherKinds extends LanguageFeature("higherKinds")
    case object Existentials extends LanguageFeature("existentials")
    case object Macros extends LanguageFeature("experimental.macros")
    case object All extends LanguageFeature("_")

    def all = List(Dynamics, PostfixOps, ReflectiveCalls,
      ImplicitConversions, HigherKinds, Existentials, Macros)
  }

  case class AddOptions(first: ScalacOptions, second: ScalacOptions) extends ScalacOptions {
    final def flags(main: ScalaMainVersion): Seq[String] =
      flattened.flatMap(_.flags(main))

    override final def flattened: Seq[ScalacOptions] =
      first.flattened ++ second.flattened
  }

  case class RemoveOptions(first: ScalacOptions, second: ScalacOptions) extends ScalacOptions {
    final def flags(main: ScalaMainVersion): Seq[String] =
      flattened.flatMap(_.flags(main))

    override final def flattened: Seq[ScalacOptions] =
      first.flattened filterNot second.flattened.toSet
  }

  class General(opts: String*) extends ScalacOptions {
    final def flags(main: ScalaMainVersion): Seq[String] = opts
  }

  trait RequireFatal {this: ScalacOptions ⇒
    override final def requires: Seq[ScalacOptions] =
      FatalWarnings :: Nil
  }

  def apply(s: ScalacOptions, bin: ScalaMainVersion, java: JavaVersion): Seq[String] =
    scalacOpts(s, bin) ++ javaTargetOpt(bin, java)

  def scalacOpts(s: ScalacOptions, bin: ScalaMainVersion): Seq[String] =
    s.finalFlags(bin)

  def javaTargetOpt(bin: ScalaMainVersion, java: JavaVersion) =
    List(s"-target:jvm-${targetJvmVersion(java, bin).jvm}")

  def targetJvmVersion(java: JavaVersion, main: ScalaMainVersion): JavaVersion =
    java match {
      case Java19 ⇒ main match {
        case Unknown  ⇒ Java15
        case Scala210 ⇒ Java17
        case _        ⇒ Java18
      }
      case Java18 ⇒ main match {
        case Unknown  ⇒ Java15
        case Scala210 ⇒ Java17
        case _        ⇒ Java18
      }
      case Java17 ⇒ main match {
        case Unknown ⇒ Java15
        case _       ⇒ Java17
      }
      case Java16 ⇒ main match {
        case Unknown ⇒ Java15
        case _       ⇒ Java16
      }
      case _      ⇒ Java15
    }
}

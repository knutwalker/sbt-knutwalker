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

import de.knutwalker.sbt.ScalaMainVersion.{Scala210, Scala211, Scala212}
import de.knutwalker.sbt.ScalacOptions.LanguageFeature.{Dynamics, Existentials, HigherKinds, ImplicitConversions, Macros, PostfixOps, ReflectiveCalls}
import de.knutwalker.sbt.ScalacOptions._
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object ScalacOptionsSpec extends Specification with ScalaCheck {

  "ScalacOptions" can {
    "be added together" in prop { (s: ScalacOptions, a: ScalacOptions) ⇒
      (s.flattened :+ a) ==== (s and a).flattened
    }.setArbitraries(arbScalacOptions, arbAtomicScalacOptions)

    "be removed from each other" in prop { (s: ScalacOptions, a: ScalacOptions) ⇒
      (s.flattened filterNot (_ == a)) ==== (s not a).flattened
    }.setArbitraries(arbScalacOptions, arbAtomicScalacOptions)


  }


  implicit val arbScalaMainVersion: Arbitrary[ScalaMainVersion] =
    Arbitrary(Gen.oneOf(Scala210, Scala211, Scala212))

  private val genAtomicScalacOptions: Gen[ScalacOptions] = Gen.oneOf(
    Lint, GoodMeasure, DisallowUnunsedCode, DisallowInferredAny, Inline,
    InlineWarnings, SimpleWarnings, ExplainTypes, Optimise, Experimental,
    FatalWarnings, DisablePredef, EliminateDeadCode, DisallowNumericWidening,
    DisallowValueDiscard, DisallowAdaptedArgs, Utf8, Dynamics, PostfixOps,
    ReflectiveCalls, ImplicitConversions, HigherKinds, Existentials, Macros
  )

  private val genAddedScalacOptions: Gen[ScalacOptions] =
    Gen.listOf(genAtomicScalacOptions) suchThat (_.nonEmpty) map { xs ⇒
      new ScalacOptions {
        def flags(main: ScalaMainVersion): Seq[String] =
          xs.flatMap(_.flags(main))

        override def flattened: Seq[ScalacOptions] = xs
      }
    }

  private val genReducedScalacOptions: Gen[ScalacOptions] = for {
    toAdd ← Gen.listOf(genAtomicScalacOptions)
    toRemove ← Gen.sized(n ⇒ Gen.listOfN(n / 2, genAtomicScalacOptions))
  } yield new ScalacOptions {
      def flags(main: ScalaMainVersion): Seq[String] =
        (toAdd filterNot toRemove.toSet).flatMap(_.flags(main))

      override def flattened: Seq[ScalacOptions] =
        toAdd filterNot toRemove.toSet
    }

  val arbAtomicScalacOptions: Arbitrary[ScalacOptions] =
    Arbitrary(genAtomicScalacOptions)

  val arbAddedScalacOptions: Arbitrary[ScalacOptions] =
    Arbitrary(genAddedScalacOptions)

  implicit val arbScalacOptions: Arbitrary[ScalacOptions] =
    Arbitrary(genReducedScalacOptions)
}

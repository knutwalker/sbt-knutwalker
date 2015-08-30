# sbt-knutwalker

An opinionated plugin for github-based open-source projects.


## Introduction

`sbt-knutwalker` is a meta-plugin for sbt to simplify project builds
with typical or recommended settings.
It's based on the way I like to build my projects and if you agree with
me there, you might benefit from this plugin, too.

## What you get

As a meta-plugin, the following plugins are automatically included

- [sbt-release](https://github.com/sbt/sbt-release/) 1.0.0
  Provides a customizable workflow for releases

- [sbt-sonatype](https://github.com/xerial/sbt-sonatype) 0.5.1
  Provides support for Sonatype

- [sbt-pgp](https://github.com/sbt/sbt-pgp) 1.0.0
  For signing artifacts when publishing to sonatype

- [sbt-header](https://github.com/sbt/sbt-header) 1.5.0
  For creating and maintaining license headers

- [sbt-scoverage](https://github.com/scoverage/sbt-scoverage) 1.2.0
  For running statement and branch coverage

- [sbt-coveralls](https://github.com/scoverage/sbt-coveralls) 1.0.0
  For uploading coverage data to coveralls.io

Further, these things are provided or simplified

- simpler releasing to sonatype
- Apache License with automatic license header creation
- [useful scalac flags](http://tpolecat.github.io/2014/04/11/scalac-flags.html)
- better configurable scalac flags in general, with support for differences of scala versions (cross compiler flags, so to speak)
- some simpler library management
- more aggresive resolver caching to avoid [repeated resolving...](https://twitter.com/datazenit/status/585540351978536962/photo/1)


## Requirements

- sbt 0.13.8
- Java 7

## Usage

Add the folling to you `project/plugins.sbt`

```scala
addSbtPlugin("de.knutwalker" % "sbt-knutwalker" % "0.2.0")
```

The following keys should be used to activate various features and simplicifications

##### `maintainer`

`maintainer := "Me Dev"`

Maintainer is used mainly for the license header as the licenser to use
(Copyright YYYY $maintainer)

It is also used as the name for the developer in the generated pom for
sonatype releasing

##### `githubProject`

`githubProject := de.knutwalker.sbt.Github("myorg", "awesome-project")`

Setting `githubProject`:
  - sets the `organizationHomepage` to the github user site
  - sets the `homepage` to the github repository site
  - fills the scm-info in the generated pom for sonatype releasing
  - sets the id and url for the developer in the generated pom for
sonatype releasing

##### `githubDevs`

A list of developers that will be included in the generated pom for
sonatype releasing.
It also workarounds a bug in pom generation in sbt 0.13.8

##### `scalacFlags`

A typely alternative to scalacOptions that allows logical grouping and
combination of some scalac options. Also, these options can abstract
over the differences in various scalas versions.

For example, the default scalacOptions are defined as

```scala
scalacFlags := {
  Lint and GoodMeasure and SimpleWarnings and Utf8 and
  LanguageFeature.Existentials and LanguageFeature.HigherKinds and
  LanguageFeature.ImplicitConversions and EliminateDeadCode and
  DisallowInferredAny and DisallowAdaptedArgs and
  DisallowNumericWidening
}
```

and `Lint` would set `-Xlint:_` in Scala 2.11 and `-Xlint` in Scala 2.10.

For now, [look at the source, luke](src/main/scala/ScalacOptions.scala).

##### `javaVersion`

`javaVersion` controls the scalac flag `target`, based on the used version
and the different target support for of different scala versions.

##### _other keys_

There are several [other keys](src/main/scala/KSbtKeys.scala) that are not yet implemented
correctly or sufficently to be mentioned here.


### Example

The following is a minimal `build.sbt` definition that includes
license headers and enables publishing to Sonatype nexus.

```scala
         name := "aweseome-project"
   maintainer := "Foo Bar"
 scalaVersion := "2.11.6"
 organization := "org.example"
githubProject := Github("example", "awesome-project")
```

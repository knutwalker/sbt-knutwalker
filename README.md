# sbt-knutwalker

An opinionated plugin for github-based open-source projects.


## Introduction

`sbt-knutwalker` is a meta-plugin for sbt to simplify project builds
with typical or recommended settings.
It's based on the way I like to build my projects and if you agree with
me there, you might benefit from this plugin, too.

## What you get

As a meta-plugin, the following plugins are automatically included (alphabetical order)

- [sbt-assembly](https://github.com/sbt/sbt-assembly) 0.14.2
  Build fat-jars for your application

- [sbt-buildinfo](https://github.com/sbt/sbt-buildinfo) 0.6.1
  Generate code based on your `build.sbt`
  
- [sbt-docker](https://github.com/marcuslonnberg/sbt-docker) 1.3.0
  Generate docker images for your application
  
- [sbt-ghpages](https://github.com/sbt/sbt-ghpages) 0.5.4
  Push documentation and static content to github pages
  
- [sbt-git](https://github.com/sbt/sbt-git) 0.8.5
  Versioning based on git

- [sbt-header](https://github.com/sbt/sbt-header) 1.5.1
  For creating and maintaining license headers

- [sbt-jmh](https://github.com/ktoso/sbt-jmh) 0.2.6
  Trust no one, bench everything
  
- [sbt-mima-plugin](https://github.com/typesafehub/migration-manager/wiki/Sbt-plugin) 0.1.9
  Check for binary compability

- [sbt-pgp](https://github.com/sbt/sbt-pgp) 1.0.0
  For signing artifacts when publishing to sonatype

- [sbt-release](https://github.com/sbt/sbt-release) 1.0.3
  Provides a customizable workflow for releases

- [sbt-revolver](https://github.com/spray/sbt-revolver) 0.8.0
  quickly restart your application

- [sbt-sonatype](https://github.com/xerial/sbt-sonatype) 1.1
  Provides support for Sonatype

- [sbt-scoverage](https://github.com/scoverage/sbt-scoverage) 1.3.5
  For running statement and branch coverage

- [sbt-site](https://github.com/sbt/sbt-site) 1.0.0
  Generate static content for documentation (and github pages)
  
- [tut-plugin](https://github.com/tpolecat/tut) 0.4.2
  Compile and check your documentation
  
- [sbt-unidoc](https://github.com/sbt/sbt-unidoc) 0.3.3
  Generate unified documentation across multiple projects

Not all of these are configured (e.g. docker), they're just included so you don't
have to add them to the `plugins.sbt`. Also, some of those are targeted at libraries
while others are targeted at applications. Future versions might make a distinction there.



Further, these things are provided or simplified

- simpler releasing to sonatype
- Apache License with automatic license header creation
- [useful scalac flags](http://tpolecat.github.io/2014/04/11/scalac-flags.html)
- more aggresive resolver caching to avoid [repeated resolving...](https://twitter.com/datazenit/status/585540351978536962/photo/1)


## Requirements

- sbt 0.13.8
- Java 7

## Usage

Add the folling to you `project/plugins.sbt`

```scala
addSbtPlugin("de.knutwalker" % "sbt-knutwalker" % "0.3.0")
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

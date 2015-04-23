package de.knutwalker.sbt

import sbt._

object Dependencies {

  def akka(v: String) = List(
    "com.typesafe.akka" %% "akka-actor" % v
  )

  def lucene(v: String) = List(
    "org.apache.lucene" % "lucene-core" % v
  )

  def netty(v: String) = List(
    "io.netty" % "netty-buffer" % v
  )

  def rxJava(v: String) = List(
    "io.reactivex" % "rxjava" % v
  )

  def rxScala(v: String) = List(
    "io.reactivex" %% "rxscala" % v
  )

  def shapeless(v: String) = List(
    "com.chuusai" %% "shapeless" % v
  )

  def scalaz(v: String) = List(
    "org.scalaz" %% "scalaz-core"       % v,
    "org.scalaz" %% "scalaz-effect"     % v,
    "org.scalaz" %% "scalaz-concurrent" % v
  )
}

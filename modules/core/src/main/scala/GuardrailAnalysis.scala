package dev.guardrail
package sbt

import _root_.sbt._
import _root_.sbt.util.CacheImplicits._
import sjsonnew.{ :*:, LList, LNil}

case class GuardrailAnalysis(guardrailVersion: String, products: Set[java.io.File]) {
  def ++(that: GuardrailAnalysis): GuardrailAnalysis =
    GuardrailAnalysis(guardrailVersion, products ++ that.products)
}
object GuardrailAnalysis {

  private val from: (String :*: Set[java.io.File] :*: LNil) => GuardrailAnalysis = {
    case ((_, version) :*: (_, in) :*: LNil) => GuardrailAnalysis(version, in)
  }

  implicit val analysisIso = LList.iso(
    { a: GuardrailAnalysis => ("guardrailVersion", a.guardrailVersion) :*: ("products", a.products) :*: LNil },
    { from })
}

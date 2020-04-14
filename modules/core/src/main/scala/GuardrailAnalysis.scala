package com.twilio.guardrail
package sbt

import _root_.sbt._
import _root_.sbt.util.CacheImplicits._
import sjsonnew.{ :*:, LList, LNil}

case class GuardrailAnalysis(products: List[java.io.File]) {
  def ++(that: GuardrailAnalysis): GuardrailAnalysis =
    GuardrailAnalysis(products ++ that.products)
}
object GuardrailAnalysis {
  implicit val analysisIso = LList.iso(
    { a: GuardrailAnalysis => ("products", a.products) :*: LNil },
    { in: List[java.io.File] :*: LNil => GuardrailAnalysis(in._1) })
}

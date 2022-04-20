package dev.guardrail.sbt

import dev.guardrail.Context

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class EscapeTreeSpec extends AnyFunSuite with Matchers {

  test("Ensure that all Context fields are accounted for") {
    val Context(
      framework,
      customExtraction,
      tracing,
      modules,
      propertyRequirement,
      tagsBehaviour,
      authImplementation
    ) = Context.empty
  }
}

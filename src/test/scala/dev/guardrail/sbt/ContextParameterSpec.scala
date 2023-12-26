package dev.guardrail.sbt

import dev.guardrail.{ AuthImplementation, Context, TagsBehaviour }

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class EscapeTreeSpec extends AnyFunSuite with Matchers {

  test("Ensure that all Context fields are accounted for") {
    val built = Context(
      None,
      customExtraction = false,
      tracing = false,
      modules = List.empty,
      propertyRequirement = dev.guardrail.terms.protocol.PropertyRequirement.Configured(
        dev.guardrail.terms.protocol.PropertyRequirement.OptionalLegacy,
        dev.guardrail.terms.protocol.PropertyRequirement.OptionalLegacy
      ),
      tagsBehaviour = TagsBehaviour.TagsAreIgnored,
      authImplementation = AuthImplementation.Disable
    )
    built.toString() shouldBe (Context.empty.toString())
  }
}

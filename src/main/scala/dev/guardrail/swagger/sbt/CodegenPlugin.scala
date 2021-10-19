package dev.guardrail
package sbt

import dev.guardrail.generators.GeneratorMappings

import _root_.sbt.AutoPlugin

object GuardrailPlugin extends AutoPlugin with AbstractGuardrailPlugin {
  def languages = GeneratorMappings.defaultLanguages

  object autoImport extends guardrailAutoImport
}

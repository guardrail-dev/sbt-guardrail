package dev.guardrail
package sbt

import dev.guardrail.cli.CLI

import _root_.sbt.AutoPlugin

object GuardrailPlugin extends AutoPlugin with AbstractGuardrailPlugin {
  def runner = CLI.guardrailRunner

  object autoImport extends guardrailAutoImport
}

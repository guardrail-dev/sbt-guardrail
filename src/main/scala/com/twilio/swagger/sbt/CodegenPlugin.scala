package com.twilio.guardrail
package sbt

import _root_.sbt.AutoPlugin

object GuardrailPlugin extends AutoPlugin with AbstractGuardrailPlugin {
  def runner = CLI.guardrailRunner

  object autoImport extends guardrailAutoImport
}

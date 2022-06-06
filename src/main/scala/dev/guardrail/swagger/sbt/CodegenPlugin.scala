package dev.guardrail
package sbt

import _root_.sbt.AutoPlugin

object GuardrailPlugin extends AutoPlugin with AbstractGuardrailPlugin {
  object autoImport extends guardrailAutoImport
}

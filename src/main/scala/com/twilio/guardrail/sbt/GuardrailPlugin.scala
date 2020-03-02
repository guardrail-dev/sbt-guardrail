package com.twilio.guardrail.sbt

import java.nio.file.Path

import cats.data.NonEmptyList
import com.twilio.guardrail.{Args, CLI, CoreTarget}
import sbt.AutoPlugin

object GuardrailPlugin extends AutoPlugin with AbstractGuardrailPlugin {
  def runner: Map[String, NonEmptyList[Args]] => CoreTarget[List[Path]] = CLI.guardrailRunner

  object autoImport extends guardrailAutoImport
}

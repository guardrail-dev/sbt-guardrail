package com.twilio.guardrail
package sbt

import java.io.File

import _root_.sbt.{ SettingKey, TaskKey }
import scala.language.implicitConversions

import com.twilio.guardrail.protocol.terms.protocol.PropertyRequirement

sealed trait CodingConfig {
  def toOptionalRequirement: PropertyRequirement.OptionalRequirement = this match {
    case CodingConfig.RequiredNullable => PropertyRequirement.RequiredNullable
    case CodingConfig.Optional => PropertyRequirement.Optional
    case CodingConfig.OptionalLegacy => PropertyRequirement.OptionalLegacy
  }
}
object CodingConfig {
  case object RequiredNullable extends CodingConfig
  case object Optional extends CodingConfig
  case object OptionalLegacy extends CodingConfig
}

object Keys {
  sealed trait SwaggerConfigValue[T] { def toOption: Option[T] }
  def Default[T]: SwaggerConfigValue[T] = SwaggerConfigValue.Default()
  def Value[T](value: T): SwaggerConfigValue[T] = SwaggerConfigValue.Value(value)
  object SwaggerConfigValue {
    case class Default[T]() extends SwaggerConfigValue[T] { def toOption = None }
    case class Value[T](value: T) extends SwaggerConfigValue[T] { def toOption = Some(value) }
    implicit def liftSwaggerConfigValue[T](value: T): SwaggerConfigValue[T] = Value(value)
  }

  val guardrailDefaults = SettingKey[Args]("guardrail-defaults")
  val guardrailTasks = SettingKey[List[Types.Args]]("guardrail-tasks")
  val guardrail = TaskKey[Seq[File]](
    "guardrail",
    "Generate swagger sources"
  )

  def codingRequiredNullable: CodingConfig = CodingConfig.RequiredNullable
  def codingOptional: CodingConfig = CodingConfig.Optional
  def codingOptionalLegacy: CodingConfig = CodingConfig.OptionalLegacy
}

package dev.guardrail
package sbt

import java.io.File

import _root_.sbt.{ SettingKey, TaskKey }
import scala.language.implicitConversions

import dev.guardrail.{AuthImplementation, Context, TagsBehaviour}
import dev.guardrail.terms.protocol.PropertyRequirement

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
  sealed trait GuardrailConfigValue[T] { def toOption: Option[T] }
  def Default[T]: GuardrailConfigValue[T] = GuardrailConfigValue.Default()
  def Value[T](value: T): GuardrailConfigValue[T] = GuardrailConfigValue.Value(value)
  object GuardrailConfigValue {
    case class Default[T]() extends GuardrailConfigValue[T] { def toOption = None }
    case class Value[T](value: T) extends GuardrailConfigValue[T] { def toOption = Some(value) }
    implicit def liftGuardrailConfigValue[T](value: T): GuardrailConfigValue[T] = Value(value)
  }

  val guardrailDefaults = SettingKey[Args]("guardrail-defaults")
  val guardrailDiscoveredOpenApiFiles = SettingKey[List[GuardrailHelpers.DiscoveredFile]]("guardrail-discovered-open-api-files")
  val guardrailTasks = SettingKey[List[Types.Args]]("guardrail-tasks")
  val guardrail = TaskKey[Seq[File]](
    "guardrail",
    "Generate source from Swagger/OpenAPI specifications"
  )

  def codingRequiredNullable: CodingConfig = CodingConfig.RequiredNullable
  def codingOptional: CodingConfig = CodingConfig.Optional
  def codingOptionalLegacy: CodingConfig = CodingConfig.OptionalLegacy

  def tagsAreIgnored: TagsBehaviour = TagsBehaviour.TagsAreIgnored
  def tagsAsPackage: TagsBehaviour = TagsBehaviour.PackageFromTags

  def authImplementationDisable: AuthImplementation = AuthImplementation.Disable
  def authImplementationNative: AuthImplementation = AuthImplementation.Native
  def authImplementationSimple: AuthImplementation = AuthImplementation.Simple
  def authImplementationCustom: AuthImplementation = AuthImplementation.Custom
}

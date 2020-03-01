package com.twilio.guardrail
package sbt

import java.io.File

import _root_.sbt.{ SettingKey, TaskKey }
import scala.language.implicitConversions

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
}

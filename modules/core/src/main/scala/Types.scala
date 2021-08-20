package dev.guardrail
package sbt

import dev.guardrail.{
  Args => ArgsImpl,
  Context => ContextImpl
}

object Types {
  type Language = String
  type Args = (Language, ArgsImpl)
  type Context = ContextImpl
}

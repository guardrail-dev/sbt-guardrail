package com.twilio.guardrail
package sbt

import com.twilio.guardrail.{
  Args => ArgsImpl,
  Context => ContextImpl
}

object Types {
  type Language = String
  type Args = (Language, ArgsImpl)
  type Context = ContextImpl
}

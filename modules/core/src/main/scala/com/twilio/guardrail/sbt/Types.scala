package com.twilio.guardrail.sbt

import java.io.File

import com.twilio.guardrail.{Args => ArgsImpl, CodegenTarget => CodegenTargetImpl, Context => ContextImpl}

object Types {

  case class PluginArgs(
    kind: CodegenTargetImpl,
    specPath: Option[File],
    outputPath: Option[String],
    packageName: Option[List[String]],
    dtoPackage: List[String],
    context: ContextImpl,
    defaults: Boolean,
    imports: List[String]
  ) {
    def toArgs: ArgsImpl = ArgsImpl.empty.copy(
      defaults = defaults,
      kind = kind,
      specPath = specPath.map(_.getPath),
      packageName = packageName,
      dtoPackage = dtoPackage,
      imports = imports,
      context = context
    )
  }

  type Language = String
  type Args = (Language, PluginArgs)
  type Context = ContextImpl
}

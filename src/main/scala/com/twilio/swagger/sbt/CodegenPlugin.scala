package com.twilio.guardrail
package sbt

import _root_.sbt.{Keys => SbtKeys, _}
import _root_.sbt.plugins.JvmPlugin
import com.twilio.guardrail.{
  Args => ArgsImpl,
  CodegenTarget => CodegenTargetImpl,
  Context => ContextImpl
}

object GuardrailPlugin extends AutoPlugin {
  override def requires = JvmPlugin
  override def trigger = allRequirements

  private[this] def impl(
      kind: CodegenTargetImpl,
      specPath: Option[java.io.File],
      packageName: Option[String],
      dtoPackage: Option[String],
      framework: Option[String],
      tracing: Option[Boolean],
      modules: List[String],
      defaults: Boolean,
      imports: List[String]
    ): ArgsImpl = {
      ArgsImpl.empty.copy(
        defaults=defaults,
        kind=kind,
        specPath=specPath.map(_.getPath),
        packageName=packageName.map(_.split('.').toList),
        dtoPackage=dtoPackage.toList.flatMap(_.split('.').filterNot(_.isEmpty).toList),
        imports=imports,
        context=ContextImpl.empty.copy(
          framework=framework,
          tracing=tracing.getOrElse(ContextImpl.empty.tracing),
          modules=modules
        ))
    }

  sealed trait ClientServer {
    val kind: CodegenTargetImpl
    val language: String

    def apply(
      specPath: java.io.File,
      pkg: String = "swagger",
      dto: Keys.SwaggerConfigValue[String] = Keys.Default,
      framework: Keys.SwaggerConfigValue[String] = Keys.Default,
      tracing: Keys.SwaggerConfigValue[Boolean] = Keys.Default,
      modules: Keys.SwaggerConfigValue[List[String]] = Keys.Default,
      imports: Keys.SwaggerConfigValue[List[String]] = Keys.Default,
    ): Types.Args = (language, impl(
      kind = kind,
      specPath = Some(specPath),
      packageName = Some(pkg),
      dtoPackage = dto.toOption,
      framework = framework.toOption,
      tracing = tracing.toOption,
      modules = modules.toOption.getOrElse(List.empty),
      imports = imports.toOption.getOrElse(List.empty),
      defaults = false
    ))

    def defaults(
      pkg: Keys.SwaggerConfigValue[String] = Keys.Default,
      dto: Keys.SwaggerConfigValue[String] = Keys.Default,
      framework: Keys.SwaggerConfigValue[String] = Keys.Default,
      tracing: Keys.SwaggerConfigValue[Boolean] = Keys.Default,
      modules: Keys.SwaggerConfigValue[List[String]] = Keys.Default,
      imports: Keys.SwaggerConfigValue[List[String]] = Keys.Default,

      // Deprecated parameters
      packageName: Keys.SwaggerConfigValue[String] = Keys.Default,
      dtoPackage: Keys.SwaggerConfigValue[String] = Keys.Default
    ): Types.Args = (language, impl(
      kind = kind,
      specPath = None,
      packageName = pkg.toOption.orElse(packageName.toOption),
      dtoPackage = dto.toOption.orElse(dtoPackage.toOption),
      framework = framework.toOption,
      tracing = tracing.toOption,
      modules = modules.toOption.getOrElse(List.empty),
      imports = imports.toOption.getOrElse(List.empty),
      defaults = true
    ))
  }


  object autoImport {
    val guardrailDefaults = Keys.guardrailDefaults
    val guardrailTasks = Keys.guardrailTasks
    val guardrail = Keys.guardrail

    @deprecated("0.45.0", "Please use ScalaClient instead")
    object Client extends ClientServer {
      val kind = CodegenTargetImpl.Client
      val language = "scala"
    }

    @deprecated("0.45.0", "Please use ScalaModels instead")
    object Models extends ClientServer {
      val kind = CodegenTargetImpl.Models
      val language = "scala"
    }

    @deprecated("0.45.0", "Please use ScalaServer instead")
    object Server extends ClientServer {
      val kind = CodegenTargetImpl.Server
      val language = "scala"
    }

    object ScalaClient extends ClientServer {
      val kind = CodegenTargetImpl.Client
      val language = "scala"
    }

    object ScalaModels extends ClientServer {
      val kind = CodegenTargetImpl.Models
      val language = "scala"
    }

    object ScalaServer extends ClientServer {
      val kind = CodegenTargetImpl.Server
      val language = "scala"
    }

    object JavaClient extends ClientServer {
      val kind = CodegenTargetImpl.Client
      val language = "java"
    }

    object JavaModels extends ClientServer {
      val kind = CodegenTargetImpl.Models
      val language = "java"
    }

    object JavaServer extends ClientServer {
      val kind = CodegenTargetImpl.Server
      val language = "java"
    }
  }

  override lazy val projectSettings = Seq(
    Keys.guardrailTasks in Compile := List.empty,
    Keys.guardrailTasks in Test := List.empty,
    Keys.guardrail in Compile := Tasks.guardrailTask((Keys.guardrailTasks in Compile).value, (SbtKeys.managedSourceDirectories in Compile).value.head),
    Keys.guardrail in Test := Tasks.guardrailTask((Keys.guardrailTasks in Test).value, (SbtKeys.managedSourceDirectories in Test).value.head),
    SbtKeys.sourceGenerators in Compile += (Keys.guardrail in Compile).taskValue,
    SbtKeys.sourceGenerators in Test += (Keys.guardrail in Test).taskValue,
    SbtKeys.watchSources in Compile ++= Tasks.watchSources((Keys.guardrailTasks in Compile).value),
    SbtKeys.watchSources in Test ++= Tasks.watchSources((Keys.guardrailTasks in Test).value),
  )
}

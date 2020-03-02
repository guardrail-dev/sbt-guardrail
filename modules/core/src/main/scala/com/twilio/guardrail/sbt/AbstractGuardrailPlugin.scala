package com.twilio.guardrail
package sbt

import java.nio.file.Path

import _root_.sbt.nio.{Keys => NioKeys}
import _root_.sbt.plugins.JvmPlugin
import _root_.sbt.{Keys => SbtKeys, _}
import cats.data.NonEmptyList
import com.twilio.guardrail.{Args => ArgsImpl, CodegenTarget => CodegenTargetImpl, Context => ContextImpl}

trait AbstractGuardrailPlugin { self: AutoPlugin =>
  def runner: Map[String, NonEmptyList[ArgsImpl]] => com.twilio.guardrail.CoreTarget[List[Path]]

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
  ): Types.PluginArgs =
    Types.PluginArgs(
      kind = kind,
      specPath = specPath,
      outputPath = None,
      packageName = packageName.map(_.split('.').toList),
      dtoPackage = dtoPackage.toList.flatMap(_.split('.').filterNot(_.isEmpty).toList),
      context = ContextImpl.empty.copy(
        framework = framework,
        tracing = tracing.getOrElse(ContextImpl.empty.tracing),
        modules = modules
      ),
      defaults = defaults,
      imports = imports
    )

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


  trait guardrailAutoImport {
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

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(Compile, Test).flatMap(conf =>
    Seq(
      conf / Keys.guardrailTasks := List.empty,
      conf / Keys.guardrail / NioKeys.fileInputs := (conf / Keys.guardrailTasks).value.flatMap(_._2.specPath).map(_.toPath.toAbsolutePath.toGlob),
      conf / Keys.guardrail := {
        val tasks = (conf / Keys.guardrailTasks).value
        val sourceDir = (conf / SbtKeys.sourceManaged).value
        val changes = (conf / Keys.guardrail).inputFileChanges
        if (!sourceDir.exists() || changes.hasChanges) Tasks.guardrailTask(runner)(tasks, sourceDir)
        else (sourceDir ** "*.scala" +++ sourceDir ** "*.java").get()
      },
      conf / SbtKeys.sourceGenerators += (conf / Keys.guardrail).taskValue,
      conf / SbtKeys.watchSources ++= Tasks.watchSources((conf / Keys.guardrailTasks).value)
    )
  )
}

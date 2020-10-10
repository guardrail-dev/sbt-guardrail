package com.twilio.guardrail
package sbt

import _root_.sbt.{Keys => SbtKeys, _}
import _root_.sbt.plugins.JvmPlugin
import com.twilio.guardrail.protocol.terms.protocol.PropertyRequirement
import com.twilio.guardrail.{
  Args => ArgsImpl,
  CodegenTarget => CodegenTargetImpl,
  Context => ContextImpl
}

trait AbstractGuardrailPlugin { self: AutoPlugin =>
  def runner: Map[String,cats.data.NonEmptyList[com.twilio.guardrail.Args]] => com.twilio.guardrail.Target[List[java.nio.file.Path]]
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
      imports: List[String],
      encodeOptionalAs: Option[CodingConfig],
      decodeOptionalAs: Option[CodingConfig],
      customExtraction: Option[Boolean],
    ): ArgsImpl = {
      val propertyRequirement = (encodeOptionalAs, decodeOptionalAs) match {
        case (None, None)       => ContextImpl.empty.propertyRequirement
        case (encoder, decoder) =>
          val fallback = ContextImpl.empty.propertyRequirement
          PropertyRequirement.Configured(
            encoder.fold(fallback.encoder)(_.toOptionalRequirement),
            decoder.fold(fallback.decoder)(_.toOptionalRequirement)
          )
      }

      ArgsImpl.empty.copy(
        defaults=defaults,
        kind=kind,
        specPath=specPath.map(_.getPath),
        packageName=packageName.map(_.split('.').toList),
        dtoPackage=dtoPackage.toList.flatMap(_.split('.').filterNot(_.isEmpty).toList),
        imports=imports,
        context=ContextImpl.empty.copy(
          customExtraction=customExtraction.getOrElse(ContextImpl.empty.customExtraction),
          framework=framework,
          tracing=tracing.getOrElse(ContextImpl.empty.tracing),
          modules=modules,
          propertyRequirement=propertyRequirement
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
      encodeOptionalAs: Keys.SwaggerConfigValue[CodingConfig] = Keys.Default,
      decodeOptionalAs: Keys.SwaggerConfigValue[CodingConfig] = Keys.Default,
      customExtraction: Keys.SwaggerConfigValue[Boolean] = Keys.Default,
    ): Types.Args = (language, impl(
      kind = kind,
      specPath = Some(specPath),
      packageName = Some(pkg),
      dtoPackage = dto.toOption,
      framework = framework.toOption,
      tracing = tracing.toOption,
      modules = modules.toOption.getOrElse(List.empty),
      imports = imports.toOption.getOrElse(List.empty),
      encodeOptionalAs = encodeOptionalAs.toOption,
      decodeOptionalAs = decodeOptionalAs.toOption,
      customExtraction = customExtraction.toOption,
      defaults = false
    ))

    def defaults(
      pkg: Keys.SwaggerConfigValue[String] = Keys.Default,
      dto: Keys.SwaggerConfigValue[String] = Keys.Default,
      framework: Keys.SwaggerConfigValue[String] = Keys.Default,
      tracing: Keys.SwaggerConfigValue[Boolean] = Keys.Default,
      modules: Keys.SwaggerConfigValue[List[String]] = Keys.Default,
      imports: Keys.SwaggerConfigValue[List[String]] = Keys.Default,
      encodeOptionalAs: Keys.SwaggerConfigValue[CodingConfig] = Keys.Default,
      decodeOptionalAs: Keys.SwaggerConfigValue[CodingConfig] = Keys.Default,
      customExtraction: Keys.SwaggerConfigValue[Boolean] = Keys.Default,

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
      encodeOptionalAs = encodeOptionalAs.toOption,
      decodeOptionalAs = decodeOptionalAs.toOption,
      customExtraction = customExtraction.toOption,
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

  private def cachedGuardrailTask(kind: String, streams: _root_.sbt.Keys.TaskStreams)(tasks: List[(String, Args)], sources: Seq[java.io.File]) = {
        import _root_.sbt.util.CacheImplicits._

        def calcResult() =
          GuardrailAnalysis(Tasks.guardrailTask(runner)(tasks, sources.head).toList)

        val cachedResult = Tracked.lastOutput[Unit, GuardrailAnalysis](streams.cacheStoreFactory.sub(kind).make("last")) {
          (_, prev) =>
          val tracker = Tracked.inputChanged[String, GuardrailAnalysis](streams.cacheStoreFactory.sub(kind).make("input")) {
            (changed: Boolean, in: String) =>
              prev match {
                case None => calcResult()
                case Some(prevResult) =>
                  if (changed) {
                    calcResult()
                  } else prevResult
              }
          }

          val inputs = tasks.flatMap(_._2.specPath.map( x => (FileInfo.hash(new java.io.File(x)))))

          tracker(new String(inputs.flatMap(_.hash).toArray))
        }

      cachedResult(()).products
  }

  def scopedSettings(name: String, scope: Configuration) = Seq(
    Keys.guardrailTasks in scope := List.empty,
    Keys.guardrail in scope := cachedGuardrailTask(name, _root_.sbt.Keys.streams.value)((Keys.guardrailTasks in scope).value, (SbtKeys.managedSourceDirectories in scope).value),
    SbtKeys.sourceGenerators in scope += (Keys.guardrail in scope).taskValue,
    SbtKeys.watchSources in scope ++= Tasks.watchSources((Keys.guardrailTasks in scope).value),
  )

  override lazy val projectSettings = {
    scopedSettings("compile", Compile) ++ scopedSettings("test", Test)
  }
}

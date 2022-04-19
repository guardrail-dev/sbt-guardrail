package dev.guardrail
package sbt

import _root_.sbt.{Keys => SbtKeys, _}
import _root_.sbt.plugins.JvmPlugin
import dev.guardrail.runner.GuardrailRunner
import dev.guardrail.terms.protocol.PropertyRequirement
import dev.guardrail.{
  Args => ArgsImpl,
  CodegenTarget => CodegenTargetImpl,
  Context => ContextImpl
}

trait AbstractGuardrailPlugin extends GuardrailRunner { self: AutoPlugin =>
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
      tagsBehaviour: Option[ContextImpl.TagsBehaviour]
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

      def kindaLens[A](member: Option[A])(proj: A => ContextImpl => ContextImpl): ContextImpl => ContextImpl = member.fold[ContextImpl => ContextImpl](identity _)(proj)

      val contextTransforms = Seq[ContextImpl => ContextImpl](
        kindaLens(customExtraction)(a => _.copy(customExtraction=a)),
        kindaLens(tracing)(a => _.copy(tracing=a)),
        kindaLens(tagsBehaviour)(a => _.copy(tagsBehaviour=a))
      )

      ArgsImpl.empty.copy(
        defaults=defaults,
        kind=kind,
        specPath=specPath.map(_.getPath),
        packageName=packageName.map(_.split('.').toList),
        dtoPackage=dtoPackage.toList.flatMap(_.split('.').filterNot(_.isEmpty).toList),
        imports=imports,
        context=contextTransforms.foldLeft(
          ContextImpl.empty.copy(
            framework=framework,
            modules=modules,
            propertyRequirement=propertyRequirement
          )
        )({ case (acc, next) => next(acc) })
      )
    }

  sealed trait ClientServer {
    val kind: CodegenTargetImpl
    val language: String

    def apply(
      specPath: java.io.File,
      pkg: String = "swagger",
      dto: Keys.GuardrailConfigValue[String] = Keys.Default,
      framework: Keys.GuardrailConfigValue[String] = Keys.Default,
      tracing: Keys.GuardrailConfigValue[Boolean] = Keys.Default,
      modules: Keys.GuardrailConfigValue[List[String]] = Keys.Default,
      imports: Keys.GuardrailConfigValue[List[String]] = Keys.Default,
      encodeOptionalAs: Keys.GuardrailConfigValue[CodingConfig] = Keys.Default,
      decodeOptionalAs: Keys.GuardrailConfigValue[CodingConfig] = Keys.Default,
      customExtraction: Keys.GuardrailConfigValue[Boolean] = Keys.Default,
      tagsBehaviour: Keys.GuardrailConfigValue[ContextImpl.TagsBehaviour] = Keys.Default,
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
      tagsBehaviour = tagsBehaviour.toOption,
      defaults = false
    ))

    def defaults(
      pkg: Keys.GuardrailConfigValue[String] = Keys.Default,
      dto: Keys.GuardrailConfigValue[String] = Keys.Default,
      framework: Keys.GuardrailConfigValue[String] = Keys.Default,
      tracing: Keys.GuardrailConfigValue[Boolean] = Keys.Default,
      modules: Keys.GuardrailConfigValue[List[String]] = Keys.Default,
      imports: Keys.GuardrailConfigValue[List[String]] = Keys.Default,
      encodeOptionalAs: Keys.GuardrailConfigValue[CodingConfig] = Keys.Default,
      decodeOptionalAs: Keys.GuardrailConfigValue[CodingConfig] = Keys.Default,
      customExtraction: Keys.GuardrailConfigValue[Boolean] = Keys.Default,
      tagsBehaviour: Keys.GuardrailConfigValue[ContextImpl.TagsBehaviour] = Keys.Default,

      // Deprecated parameters
      packageName: Keys.GuardrailConfigValue[String] = Keys.Default,
      dtoPackage: Keys.GuardrailConfigValue[String] = Keys.Default
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
      tagsBehaviour = tagsBehaviour.toOption,
      defaults = true
    ))
  }

  trait guardrailAutoImport {
    val guardrailDefaults = Keys.guardrailDefaults
    val guardrailTasks = Keys.guardrailTasks
    val guardrail = Keys.guardrail

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

    def codingRequiredNullable = Keys.codingRequiredNullable
    def codingOptional = Keys.codingOptional
    def codingOptionalLegacy = Keys.codingOptionalLegacy

    def tagsAreIgnored = Keys.tagsAreIgnored
    def tagsAsPackage = Keys.tagsAsPackage
  }

  private def cachedGuardrailTask(projectName: String, scope: String, scalaBinaryVersion: String)(kind: String, streams: _root_.sbt.Keys.TaskStreams)(tasks: List[(String, Args)], sources: Seq[java.io.File]) = {
    import _root_.sbt.util.CacheImplicits._

    if (BuildInfo.organization == "com.twilio" && tasks.nonEmpty) {
      streams.log.warn(s"""${projectName} / ${scope}: sbt-guardrail has changed organizations! Please change "com.twilio" to "dev.guardrail" to continue receiving updates""")
    }

    def calcResult() =
      GuardrailAnalysis(BuildInfo.version, Tasks.guardrailTask(guardrailRunner)(tasks, sources.head).toList)

    val cachedResult = Tracked.lastOutput[Unit, GuardrailAnalysis](streams.cacheStoreFactory.sub("guardrail").sub(scalaBinaryVersion).sub(kind).make("last")) {
      (_, prev) =>
      val tracker = Tracked.inputChanged[String, GuardrailAnalysis](streams.cacheStoreFactory.sub("guardrail").sub(scalaBinaryVersion).sub(kind).make("input")) {
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
    scope / Keys.guardrailTasks := List.empty,
    scope / Keys.guardrail := cachedGuardrailTask(SbtKeys.name.value, scope.name, SbtKeys.scalaBinaryVersion.value)(name, _root_.sbt.Keys.streams.value)((scope / Keys.guardrailTasks).value, (scope / SbtKeys.managedSourceDirectories).value),
    scope / SbtKeys.sourceGenerators += (scope / Keys.guardrail).taskValue,
    scope / SbtKeys.watchSources ++= Tasks.watchSources((scope / Keys.guardrailTasks).value),
  )

  override lazy val projectSettings = {
    scopedSettings("compile", Compile) ++ scopedSettings("test", Test)
  }
}

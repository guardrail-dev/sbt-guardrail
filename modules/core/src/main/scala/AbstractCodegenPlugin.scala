package dev.guardrail
package sbt

import _root_.sbt.{Keys => SbtKeys, Types => _, _}
import _root_.sbt.plugins.JvmPlugin
import dev.guardrail.runner.GuardrailRunner
import dev.guardrail.terms.protocol.PropertyRequirement
import dev.guardrail.{
  Args => ArgsImpl,
  AuthImplementation,
  CodegenTarget => CodegenTargetImpl,
  Context => ContextImpl,
  TagsBehaviour
}

trait AbstractGuardrailPlugin { self: AutoPlugin =>
  val runner = new GuardrailRunner {}
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
      tagsBehaviour: Option[TagsBehaviour],
      authImplementation: Option[AuthImplementation]
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
        kindaLens(authImplementation)(a => _.withAuthImplementation(a)),
        kindaLens(customExtraction)(a => _.withCustomExtraction(a)),
        kindaLens(tagsBehaviour)(a => _.withTagsBehaviour(a)),
        kindaLens(tracing)(a => _.withTracing(a))
      )

      ArgsImpl.empty
        .withDefaults(defaults)
        .withKind(kind)
        .withSpecPath(specPath.map(_.getPath))
        .withPackageName(packageName.map(_.split('.').toList))
        .withDtoPackage(dtoPackage.toList.flatMap(_.split('.').filterNot(_.isEmpty).toList))
        .withImports(imports)
        .withContext(contextTransforms.foldLeft(
          ContextImpl.empty
            .withFramework(framework)
            .withModules(modules)
            .withPropertyRequirement(propertyRequirement)
        )({ case (acc, next) => next(acc) }))
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
      tagsBehaviour: Keys.GuardrailConfigValue[TagsBehaviour] = Keys.Default,
      authImplementation: Keys.GuardrailConfigValue[AuthImplementation] = Keys.Default,
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
      authImplementation = authImplementation.toOption,
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
      tagsBehaviour: Keys.GuardrailConfigValue[TagsBehaviour] = Keys.Default,
      authImplementation: Keys.GuardrailConfigValue[AuthImplementation] = Keys.Default,

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
      authImplementation = authImplementation.toOption,
      defaults = true
    ))
  }

  trait guardrailAutoImport {
    val guardrailDefaults = Keys.guardrailDefaults
    val guardrailDiscoveredOpenApiFiles = Keys.guardrailDiscoveredOpenApiFiles
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

    def authImplementationDisable = Keys.authImplementationDisable
    def authImplementationNative = Keys.authImplementationNative
    def authImplementationSimple = Keys.authImplementationSimple
    def authImplementationCustom = Keys.authImplementationCustom

    lazy val GuardrailHelpers = _root_.dev.guardrail.sbt.GuardrailHelpers
  }

  private def cachedGuardrailTask(projectName: String, scope: String, scalaBinaryVersion: String)(kind: String, streams: _root_.sbt.Keys.TaskStreams)(tasks: List[(String, Args)], sources: Seq[java.io.File]) = {
    val inputFiles = tasks.flatMap(_._2.specPath).map(file(_)).toSet
    val cacheDir = streams.cacheDirectory / "guardrail" / scalaBinaryVersion / kind

    val cachedFn = _root_.sbt.util.FileFunction
      .cached(cacheDir, inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
        _ =>
          GuardrailAnalysis(
            BuildInfo.version,
            Tasks.guardrailTask(runner.guardrailRunner)(tasks, sources.head)
          ).products
      }

    cachedFn(inputFiles).toSeq
  }

  def scopedSettings(name: String, scope: Configuration) = {
    import _root_.sbt.Keys.{resourceDirectory, sourceDirectory, unmanagedResourceDirectories, unmanagedSourceDirectories}
    Seq(
      scope / unmanagedSourceDirectories += (scope / sourceDirectory).value / "openapi",
      scope / unmanagedResourceDirectories += (scope / resourceDirectory).value / "openapi",
      scope / Keys.guardrailDiscoveredOpenApiFiles := GuardrailHelpers.discoverOpenApiFiles((scope / sourceDirectory).value / "openapi"),
      scope / Keys.guardrailTasks := List.empty,
      scope / Keys.guardrail := cachedGuardrailTask(SbtKeys.name.value, scope.name, SbtKeys.scalaBinaryVersion.value)(name, _root_.sbt.Keys.streams.value)((scope / Keys.guardrailTasks).value, (scope / SbtKeys.managedSourceDirectories).value),
      scope / SbtKeys.sourceGenerators += (scope / Keys.guardrail).taskValue,
      scope / SbtKeys.watchSources ++= Tasks.watchSources((scope / Keys.guardrailTasks).value),
    )
  }

  override lazy val projectSettings = {
    scopedSettings("compile", Compile) ++ scopedSettings("test", Test)
  }
}

package com.twilio.guardrail
package sbt

import java.io.File
import java.nio.file.Path

import _root_.sbt.{FeedbackProvidedException, WatchSource}
import cats.data.NonEmptyList
import io.swagger.parser.SwaggerParserExtension

import scala.io.AnsiColor

class CodegenFailedException extends FeedbackProvidedException

object Tasks {
  def guardrailTask(
    runner: Map[String,NonEmptyList[com.twilio.guardrail.Args]] => CoreTarget[List[Path]]
  )(tasks: Seq[Types.Args], sourceDir: File): Seq[File] = {
    // swagger-parser uses SPI to find extensions on the classpath (by default, only the OAPI2 -> OAPI3 converter)
    // See https://github.com/swagger-api/swagger-parser#extensions
    // That being said, Scala's classloader seems to have some issues finding SPI resources:
    // - https://github.com/scala/bug/issues/10247
    // - https://github.com/meetup/sbt-openapi/blob/363fa14/src/main/scala/com/meetup/sbtopenapi/Plugin.scala#L64-L71
    // As a result, we temporarily overwrite Scala's classloader to whichever one loaded swagger-parser, in the hopes
    // that it'll pick up the rest of the SPI classes.
    val oldClassLoader = Thread.currentThread().getContextClassLoader()
    Thread.currentThread().setContextClassLoader(classOf[SwaggerParserExtension].getClassLoader)

    val preppedTasks = tasks.groupBy(_._1).flatMap { case (language, langArgs) =>
      NonEmptyList.fromList(langArgs.map {
        case (_, pluginArgs) => pluginArgs.toArgs.copy(outputPath = Some(sourceDir.getPath))
      }.toList).map(language -> _)
    }

    val /*(logger,*/ paths/*)*/ =
      runner
        .apply(preppedTasks)
        .fold[List[Path]]({
          case MissingArg(args, Error.ArgName(arg)) =>
            println(s"${AnsiColor.RED}Missing argument:${AnsiColor.RESET} ${AnsiColor.BOLD}${arg}${AnsiColor.RESET} (In block ${args})")
            throw new CodegenFailedException()
          case NoArgsSpecified =>
            List.empty
          case NoFramework =>
            println(s"${AnsiColor.RED}No framework specified${AnsiColor.RESET}")
            throw new CodegenFailedException()
          case PrintHelp =>
            List.empty
          case UnknownArguments(args) =>
            println(s"${AnsiColor.RED}Unknown arguments: ${args.mkString(" ")}${AnsiColor.RESET}")
            throw new CodegenFailedException()
          case UnparseableArgument(name, message) =>
            println(s"${AnsiColor.RED}Unparseable argument ${name}: ${message}${AnsiColor.RESET}")
            throw new CodegenFailedException()
          case UnknownFramework(name) =>
            println(s"${AnsiColor.RED}Unknown framework specified: ${name}${AnsiColor.RESET}")
            throw new CodegenFailedException()
          case RuntimeFailure(message) =>
            println(s"${AnsiColor.RED}Error:${AnsiColor.RESET}${message}")
            throw new CodegenFailedException()
          case UserError(message) =>
            println(s"${AnsiColor.RED}Error:${AnsiColor.RESET}${message}")
            throw new CodegenFailedException()
          case MissingModule(section) =>
            println(s"${AnsiColor.RED}Error: Missing module ${section}${AnsiColor.RESET}")
            throw new CodegenFailedException()
          case ModuleConflict(section) =>
            println(s"${AnsiColor.RED}Error: Too many modules specified for ${section}${AnsiColor.RESET}")
            throw new CodegenFailedException()
          case UnconsumedModules(modules) =>
            println(s"${AnsiColor.RED}Error: Unconsumed modules: ${modules.mkString(", ")}${AnsiColor.RESET}")
            throw new CodegenFailedException()
        }, identity)
        //.runEmpty

    Thread.currentThread().setContextClassLoader(oldClassLoader)
    paths.map(_.toFile).distinct
  }

  def watchSources(tasks: Seq[Types.Args]): Seq[WatchSource] =
    tasks.flatMap(_._2.specPath.map(WatchSource(_)))
}

package com.twilio.guardrail
package sbt

import _root_.sbt.{FeedbackProvidedException, WatchSource}
import cats.data.{EitherT, NonEmptyList, WriterT}
import cats.free.Free
import cats.implicits._
import cats.~>
import com.twilio.guardrail.core.CoreTermInterp
import com.twilio.guardrail.languages.{ ScalaLanguage, LA }
import com.twilio.guardrail.terms.{CoreTerm, CoreTerms, GetDefaultFramework}
import com.twilio.guardrail.{Common, CoreTarget}
import scala.io.AnsiColor
import scala.language.higherKinds
import scala.meta._

class CodegenFailedException extends FeedbackProvidedException

object Tasks {
  def guardrailTask(tasks: List[GuardrailPlugin.Args], sourceDir: java.io.File): Seq[java.io.File] = {
    val preppedTasks = tasks.map(_.copy(outputPath=Some(sourceDir.getPath)))
    val result = runM[ScalaLanguage, CoreTerm[ScalaLanguage, ?]](preppedTasks).foldMap(CoreTermInterp[ScalaLanguage](
      "akka-http", {
        case "akka-http" => com.twilio.guardrail.generators.AkkaHttp
        case "http4s"    => com.twilio.guardrail.generators.Http4s
      }, {
        _.parse[Importer].toEither.bimap(err => UnparseableArgument("import", err.toString), importer => Import(List(importer)))
      }
    )).fold[List[ReadSwagger[Target[List[WriteTree]]]]]({
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
      }, _.toList)

    val (coreLogger, deferred) = result.run

    val (logger, paths) = deferred
      .traverse({ rs =>
        ReadSwagger
          .readSwagger(rs)
          .fold(
            { err =>
              println(s"${AnsiColor.RED}${err}${AnsiColor.RESET}")
              throw new CodegenFailedException()
            },
            _.fold(
              {
                case err =>
                  println(s"${AnsiColor.RED}Error: ${err}${AnsiColor.RESET}")
                  throw new CodegenFailedException()
              },
              _.map(WriteTree.unsafeWriteTree)
            )
          )
      })
      .map(_.flatten)
      .run

    paths.toList.map(_.toFile).distinct
  }

  def watchSources(tasks: List[GuardrailPlugin.Args]): Seq[WatchSource] = {
    tasks.flatMap(_.specPath.map(new java.io.File(_)).map(WatchSource(_))).toSeq
  }

  private[this] def runM[L <: LA, F[_]](args: List[GuardrailPlugin.Args])(implicit C: CoreTerms[L, F]): Free[F, NonEmptyList[ReadSwagger[Target[List[WriteTree]]]]] = {
    import C._

    for {
      defaultFramework <- getDefaultFramework
      args <- validateArgs(args)
      writeTrees <- Common.processArgs(args)
    } yield writeTrees
  }
}

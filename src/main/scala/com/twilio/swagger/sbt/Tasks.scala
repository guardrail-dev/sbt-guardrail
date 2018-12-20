package com.twilio.guardrail
package sbt

import cats.~>
import cats.free.Free
import cats.data.{EitherT, NonEmptyList, WriterT}
import cats.instances.all._
import com.twilio.guardrail.{Common, CoreTarget}
import com.twilio.guardrail.core.CoreTermInterp
import com.twilio.guardrail.terms.{CoreTerm, CoreTerms, GetDefaultFramework}
import scala.language.higherKinds
import scala.io.AnsiColor
import _root_.sbt.{FeedbackProvidedException, WatchSource}

class CodegenFailedException extends FeedbackProvidedException

object Tasks {
  def guardrailTask(tasks: List[GuardrailPlugin.Args], sourceDir: java.io.File): Seq[java.io.File] = {
    val preppedTasks = tasks.map(_.copy(outputPath=Some(sourceDir.getPath)))
    runM[CoreTerm](preppedTasks).foldMap(CoreTermInterp)
      .fold({
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
      }, _.toList.flatMap({ rs =>
        EitherT.fromEither[Logger](ReadSwagger.readSwagger(rs))
        .flatMap(identity)
        .fold({ err =>
            println(s"${AnsiColor.RED}Error: ${err}${AnsiColor.RESET}")
            throw new CodegenFailedException()
          }, _.map(WriteTree.unsafeWriteTreeLogged).map(_.value.toFile))
          .value
      })).value.distinct
  }

  def watchSources(tasks: List[GuardrailPlugin.Args]): Seq[WatchSource] = {
    tasks.flatMap(_.specPath.map(new java.io.File(_)).map(WatchSource(_))).toSeq
  }

  private[this] def runM[F[_]](args: List[GuardrailPlugin.Args])(implicit C: CoreTerms[F]): Free[F, NonEmptyList[ReadSwagger[Target[List[WriteTree]]]]] = {
    import C._

    for {
      defaultFramework <- getDefaultFramework
      args <- validateArgs(args)
      writeTrees <- Common.processArgs(args)
    } yield writeTrees
  }
}

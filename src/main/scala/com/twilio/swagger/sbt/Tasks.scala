package com.twilio.guardrail
package sbt

import cats.~>
import cats.free.Free
import cats.data.NonEmptyList
import cats.instances.all._
import com.twilio.guardrail.{Common, CoreTarget}
import com.twilio.guardrail.core.CoreTermInterp
import com.twilio.guardrail.terms.{CoreTerm, CoreTerms, GetDefaultFramework}
import scala.language.higherKinds
import scala.io.AnsiColor
import _root_.sbt.WatchSource

object Compat {
  import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path, StandardOpenOption}
  val unsafeWriteTree: WriteTree => Path = { case WriteTree(path, tree) =>
    val UTF8 = java.nio.charset.Charset.availableCharsets.get("UTF-8")
    val data = tree.syntax.getBytes(UTF8)
    Files.createDirectories(path.getParent)
    if (Files.exists(path)) {
      val exists: Array[Byte] = Files.readAllBytes(path)
      val diffIdx: Option[Int] =
        exists.zip(data).zipWithIndex
          .find({ case ((a, b), _) => a != b })
          .map(_._2)
          .orElse(Some(Math.max(exists.length, data.length)))
          .filterNot(Function.const(data.length == exists.length))

      diffIdx.foreach { diffIdx =>
          val existSample = new String(exists, UTF_8).drop(Math.max(diffIdx - 10, diffIdx)).take(50).replace("\n", "\\n")
          val newSample = new String(data, UTF_8).drop(Math.max(diffIdx - 10, diffIdx)).take(50).replace("\n", "\\n")

          System.err.println(s"""|
          |${AnsiColor.RED}Warning:${AnsiColor.RESET}
          |  The file ${path} contained different content than was expected.
          |
          |  Existing file: ${existSample}
          |  New file     : ${newSample}
          |""".stripMargin)
      }
    }
    Files.write(
      path, data
      , StandardOpenOption.CREATE
      , StandardOpenOption.TRUNCATE_EXISTING
    )
  }
}


object Tasks {
  def guardrailTask(tasks: List[GuardrailPlugin.Args], sourceDir: java.io.File): Seq[java.io.File] = {
    val preppedTasks = tasks.map(_.copy(outputPath=Some(sourceDir.getPath)))
    runM[CoreTerm](preppedTasks).foldMap(CoreTermInterp)
      .fold({
        case MissingArg(args, Error.ArgName(arg)) =>
          println(s"${AnsiColor.RED}Missing argument:${AnsiColor.RESET} ${AnsiColor.BOLD}${arg}${AnsiColor.RESET} (In block ${args})")
          unsafePrintHelp()
        case NoArgsSpecified =>
          // Should be debug-only println(s"${AnsiColor.RED}No arguments specified${AnsiColor.RESET}")
          unsafePrintHelp()
        case NoFramework =>
          println(s"${AnsiColor.RED}No framework specified${AnsiColor.RESET}")
          unsafePrintHelp()
        case PrintHelp =>
          unsafePrintHelp()
        case UnknownArguments(args) =>
          println(s"${AnsiColor.RED}Unknown arguments: ${args.mkString(" ")}${AnsiColor.RESET}")
          unsafePrintHelp()
        case UnparseableArgument(name, message) =>
          println(s"${AnsiColor.RED}Unparseable argument ${name}: ${message}${AnsiColor.RESET}")
          unsafePrintHelp()
        case UnknownFramework(name) =>
          println(s"${AnsiColor.RED}Unknown framework specified: ${name}${AnsiColor.RESET}")
          List.empty
      }, _.toList.flatMap({ rs =>
        ReadSwagger.unsafeReadSwagger(rs)
          .fold({ err =>
            println(s"${AnsiColor.RED}Error: ${err}${AnsiColor.RESET}")
            unsafePrintHelp()
          }, _.map(Compat.unsafeWriteTree).map(_.toFile))
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

  private[this] def unsafePrintHelp(): List[java.io.File] = List.empty
}

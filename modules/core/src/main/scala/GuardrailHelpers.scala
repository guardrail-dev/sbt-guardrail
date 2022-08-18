package dev.guardrail
package sbt

import _root_.sbt._

object GuardrailHelpers {

  private def dropExtension(filePath: String): String = {
    filePath.split('.').toList match {
      case l@List() => l
      case l@List(_) => l
      case l => l.dropRight(1)
    }
  }.mkString(".")

  private def recursiveListFiles(f: File): List[File] = {
    val these = Option(f.listFiles).toList.flatten
    these.filter(f => f.isFile) ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

  def isOpenApiSpec(file: File): Boolean = {
    import org.snakeyaml.engine.v2.api.{Load, LoadSettings}

    import java.util.{Map => JMap}
    import scala.io.Source
    import scala.jdk.CollectionConverters._
    import scala.util.{Try, Using}

    Using(Source.fromFile(file)) { source =>
      val reader = source.bufferedReader()
      val docs = new Load(LoadSettings.builder().setAllowDuplicateKeys(true).setAllowRecursiveKeys(true).build()).loadAllFromReader(reader).asScala
      val yamls = docs.flatMap(d => Try(d.asInstanceOf[JMap[String, Any]].asScala).toOption).toList
      yamls.exists(m => m.contains("openapi") || m.contains("swagger"))
    }.toOption.getOrElse(false)
  }

  case class DiscoveredFile(base: File, file: File, fileRelative: File) {
    val fileRelativePath: String = fileRelative.getPath
    val fileRelativePathWithoutExtension: String = dropExtension(fileRelative.getPath)
    val pkg: String = fileRelativePathWithoutExtension.replace(Path.sep, '.')
  }

  def discoverFiles(base: File): List[DiscoveredFile] = recursiveListFiles(base).flatMap { file =>
    file.relativeTo(base).map { fileRelative =>
      DiscoveredFile(base, file, fileRelative)
    }
  }

  def discoverOpenApiFiles(base: File): List[DiscoveredFile] =
     discoverFiles(base).filter(f => isOpenApiSpec(f.file))

  def createGuardrailTasks(
                            sourceDirectory: File,
                          )(discoveredFileToTasks: DiscoveredFile => List[dev.guardrail.sbt.Types.Args]): List[dev.guardrail.sbt.Types.Args] =
    discoverOpenApiFiles(sourceDirectory).flatMap(discoveredFileToTasks)

}
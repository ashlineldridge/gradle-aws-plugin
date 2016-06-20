package awsplugin

import org.gradle.api.{DefaultTask, InvalidUserDataException}

import scala.collection.JavaConverters._

trait AWSTask extends DefaultTask {

  def usage: String

  def projectProperties: Map[String, String] =
    getProject.getProperties.asScala.toMap.mapValues(_.toString)

  def raiseUserError(message: String) =
    throw new InvalidUserDataException(s"Error: $message\n$usage")
}

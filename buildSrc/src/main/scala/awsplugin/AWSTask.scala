package awsplugin

import org.gradle.api.{DefaultTask, InvalidUserCodeException, InvalidUserDataException}

import scala.collection.JavaConverters._

trait AWSTask extends DefaultTask {

  def usage: String

  def projectProperties: Map[String, String] =
    getProject.getProperties.asScala.toMap.mapValues(v => if (v != null) v.toString else null)

  def raiseBuildScriptError(message: String) =
    throw new InvalidUserCodeException(s"Error: $message")

  def raiseUsageError(message: String) =
    throw new InvalidUserDataException(s"Error: $message\n$usage")

  def pluginOptions: AWSPluginOptions =
    getProject.getExtensions.getByType(classOf[AWSPluginOptions])

  protected val logger = getLogger
}

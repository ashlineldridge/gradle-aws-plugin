package awsplugin.cloudformation.tasks

import awsplugin.AWSTask
import awsplugin.cloudformation.Stack
import awsplugin.cloudformation.Stack.Environment
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient
import org.gradle.api.internal.FactoryNamedDomainObjectContainer

trait StackTask extends AWSTask {

  val client = new AmazonCloudFormationClient()

  def targetStackName: String =
    projectProperties.getOrElse("stack.name", raiseUsageError("Parameter stack.name not specified"))

  def targetStack: Stack = findStack(targetStackName).getOrElse(raiseUsageError(s"Stack '${targetStackName}' is not defined"))

  def targetEnvironment: Option[Environment] =
    projectProperties.get("stack.environment")

  def findStack(name: String): Option[Stack] =
    Option(stacksContainer.findByName(name))

  private def stacksContainer: FactoryNamedDomainObjectContainer[Stack] =
    getProject.getExtensions.getByName("stacks").asInstanceOf[FactoryNamedDomainObjectContainer[Stack]]
}

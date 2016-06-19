package awsplugin.cloudformation.tasks

import awsplugin.AWSTask
import awsplugin.cloudformation.Stack
import org.gradle.api.internal.FactoryNamedDomainObjectContainer

trait StackTask extends AWSTask {

  def targetStackName: String =
    projectProperties.getOrElse("stack.name", raiseUserError("Parameter stack.name not specified"))

  def targetStack: Stack = findStack(targetStackName)

  def findStack(name: String): Stack =
    Option(stacksContainer.findByName(name)).getOrElse(raiseUserError(s"Stack '$name' is not defined"))

  private def stacksContainer: FactoryNamedDomainObjectContainer[Stack] =
    getProject.getExtensions.getByName("stacks").asInstanceOf[FactoryNamedDomainObjectContainer[Stack]]
}

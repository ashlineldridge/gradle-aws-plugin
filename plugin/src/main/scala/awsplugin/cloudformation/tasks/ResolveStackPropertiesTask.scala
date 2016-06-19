package awsplugin.cloudformation.tasks

import org.gradle.api.tasks.TaskAction

class ResolveStackPropertiesTask extends StackTask {

  override def usage: String =
    """Usage: gradle resolveStackProperties
      |-Pstack.name=<value>
      |[-Pstack.environment=<value>]
      |[-Pstack.<name>.<propertyKey>=<value> ...]
      |[-Pstack.<name>.<environment>.<propertyKey>=<value> ...]""".stripMargin

  @TaskAction
  def run(): Unit = {
    print("Running...")
  }
}

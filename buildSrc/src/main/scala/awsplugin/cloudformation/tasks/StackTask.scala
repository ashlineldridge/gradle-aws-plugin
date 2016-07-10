package awsplugin.cloudformation.tasks

import awsplugin.AWSTask
import awsplugin.cloudformation.Stack
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient

object StackTask {
  private[tasks] var targetStack: Option[Stack] = None
}

trait StackTask extends AWSTask {

  val client = new AmazonCloudFormationClient()

  def targetStack = StackTask.targetStack getOrElse raiseBuildScriptError("ResolveStackParametersTask must be run to resolve target stack")
}

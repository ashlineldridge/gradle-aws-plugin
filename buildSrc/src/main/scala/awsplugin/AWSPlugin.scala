package awsplugin

import awsplugin.cloudformation.Stack
import awsplugin.cloudformation.tasks.ResolveStackPropertiesTask
import org.gradle.api.{Plugin, Project}

import scala.collection.JavaConverters._

class AWSPlugin extends Plugin[Project] {

  override def apply(project: Project): Unit = {
    val stacks = project.container(classOf[Stack])
    project.getExtensions.add("stacks", stacks)
    project.task(Map("type" -> classOf[ResolveStackPropertiesTask]).asJava, "resolveStackProperties")

    project.getExtensions.create("aws", classOf[AWSPluginOptions])

//    project.task("createStack", type: CreateStackTask, dependsOn: "resolveStackProperties")
//    project.task("createOrUpdateStack", type: CreateOrUpdateStackTask, dependsOn: "resolveStackProperties")
//    project.task("deleteStack", type: DeleteStackTask)
//    project.task("deleteStacks", type: DeleteStackTask)
//
//    project.task("waitUntilStackCreated", type: WaitUntilStackCreatedTask, dependsOn: "createStack")
//    project.task("waitUntilStackCreatedOrUpdated", type: WaitUntilStackCreatedOrUpdatedTask, dependsOn: "createOrUpdateStack")
//    project.task("waitUntilStackDeleted", type: WaitUntilStackDeletedTask, dependsOn: "deleteStack")
  }
}

class AWSPluginOptions {
  private[awsplugin] var region: Option[String] = None
  def setRegion(r: String) = region = Some(r)
}


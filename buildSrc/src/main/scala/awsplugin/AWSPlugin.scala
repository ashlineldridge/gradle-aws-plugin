package awsplugin

import awsplugin.cloudformation.dsl.GradleStack
import awsplugin.cloudformation.tasks.ResolveStackParametersTask
import com.amazonaws.regions.{Region, Regions}
import org.gradle.api.{Plugin, Project}

import scala.collection.JavaConverters._

class AWSPlugin extends Plugin[Project] {

  override def apply(project: Project): Unit = {
    val stacks = project.container(classOf[GradleStack])
    project.getExtensions.add("stacks", stacks)
    project.task(Map("type" -> classOf[ResolveStackParametersTask]).asJava, "resolveStackParameters")

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
  private[awsplugin] var region: Option[Region] = None
  def setRegion(r: String) = region = Some(Region.getRegion(Regions.fromName(r)))
}


package awsplugin.cloudformation.tasks

import awsplugin.cloudformation._
import com.amazonaws.regions.Region
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest
import org.gradle.api.tasks.TaskAction

import scala.collection.JavaConverters._

class ResolveStackPropertiesTask extends StackTask {

  override def usage: String =
    """Usage: gradle resolveStackProperties
      |-Pstack.name=<value>
      |[-Pstack.environment=<value>]
      |[-Pstack.<name>.<propertyKey>=<value> ...]
      |[-Pstack.<name>.<environment>.<propertyKey>=<value> ...]""".stripMargin

  @TaskAction
  def run(): Unit = {
    applyRegion(targetStack)
    applyPropertyOverrides(targetStack)
    targetStack.props.transform((k, v) => resolve(v))
    log(targetStack)
  }

  private def resolve(v: PropertyValue): SimplePropertyValue = v match {
    case x: SimplePropertyValue => x
    case y: ReferencePropertyValue =>
      val (stackName, outputKey, region) = parse(y)
      client.setRegion(region)
      val req = new DescribeStacksRequest().withStackName(stackName)
      val res = client.describeStacks(req)
      val output = res.getStacks.get(0).getOutputs.asScala.find(_.getOutputKey == outputKey)
      output.map(o => SimplePropertyValue(o.getOutputValue)).getOrElse(raiseBuildScriptError(s"Output key '${outputKey}' does not exist for stack '${stackName}'"))
  }

  private def parse(v: ReferencePropertyValue): (String, String, Region) = {
    if (v.refs.length != 3 || v.refs(1) != "output") raiseBuildScriptError(s"Property '${v}' is not a valid stack output reference")
    val stack = findStack(v.refs(0))
    val stackName = stack.map(_.qualifiedName).getOrElse(v.refs(0))
    val region = stack.map(regionFor(_)).getOrElse(targetStack.region)
    val outputKey = v.refs(2).capitalize
    (stackName, outputKey, region)
  }

  private def applyPropertyOverrides(stack: Stack) = {
    val overrides = projectProperties.filterKeys(_.startsWith(s"stack.${stack.name}"))
    overrides.foreach(kv => {
      val splitted = kv._1.split(".")
      val pk = splitted.length match {
        case 3 => PropertyKey(splitted(2))
        case 4 => PropertyKey(splitted(3), Some(splitted(2)))
        case _ => raiseUsageError(s"Invalid stack property '${kv._1}'")
      }
      stack.props.put(pk, SimplePropertyValue(kv._2))
    })
  }

  private def applyRegion(stack: Stack) =
    stack.region = regionFor(stack)

  private def regionFor(stack: Stack): Region = stack.region match {
    case null => pluginOptions.region match {
      case Some(x) => x
      case _       => raiseBuildScriptError(s"No region could be found for stack '${stack.name}'")
    }
    case r => r
  }

  private def log(stack: Stack) = {
    def printMap(m: Map[String, String]) = m.foldLeft("") { (z, a) => s"${z}\n    ${a._1}: ${a._2}" }
    logger.lifecycle(
    s"""Resolved properties for stack '${stack.name}':
       |  Qualified name: ${stack.qualifiedName}
       |  Region: ${stack.region}
       |  Tags: ${printMap(stack.tags(targetEnvironment))}
       |  Properties: ${printMap(stack.props(targetEnvironment))}""".stripMargin)
  }
}

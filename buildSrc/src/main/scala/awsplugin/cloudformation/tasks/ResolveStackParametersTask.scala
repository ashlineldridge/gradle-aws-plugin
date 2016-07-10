package awsplugin.cloudformation.tasks

import awsplugin.cloudformation.Stack
import awsplugin.cloudformation.dsl.GradleStack._
import awsplugin.cloudformation.dsl._
import com.amazonaws.regions.Region
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest
import org.gradle.api.internal.FactoryNamedDomainObjectContainer
import org.gradle.api.tasks.TaskAction

import scala.collection.JavaConverters._

class ResolveStackParametersTask extends StackTask {

  override def usage: String =
    """Usage: gradle resolveStackProperties
      |-Pstack.name=<value>
      |[-Pstack.environment=<value>]
      |[-Pstack.<name>.<propertyKey>=<value> ...]
      |[-Pstack.<name>.<environment>.<propertyKey>=<value> ...]""".stripMargin

  @TaskAction
  def run(): Unit = {
    val stack = transform(targetGradleStack)
    StackTask.targetStack = Some(stack)
    log(stack)
  }

  private def transform(stack: GradleStack): Stack = {
    val allStackParams = stack.params.toMap ++ commandLineParams(stack) mapValues resolveParamValue(stack)
    val params = byEnvironment(allStackParams, targetEnvironment)
    val tags = byEnvironment(stack.tags.toMap, targetEnvironment)
    Stack(stack.name, stack.qualifiedName, region(stack), params, tags, stack.template)
  }

  private def resolveParamValue(stack: GradleStack)(v: ParamValue): SimpleParamValue = v match {
    case x: SimpleParamValue => x
    case y: ReferenceParamValue =>
      val (stackName, outputKey, stackRegion) = parse(y)
      client.setRegion(stackRegion getOrElse region(stack))
      val req = new DescribeStacksRequest().withStackName(stackName)
      val res = client.describeStacks(req)
      val output = res.getStacks.get(0).getOutputs.asScala.find(_.getOutputKey == outputKey)
      output.map(o => SimpleParamValue(o.getOutputValue)).getOrElse(raiseBuildScriptError(s"Output key '${outputKey}' does not exist for stack '${stackName}'"))
  }

  private def parse(v: ReferenceParamValue): (String, String, Option[Region]) = {
    if (v.refs.length != 3 || v.refs(1) != "output") raiseBuildScriptError(s"Property '${v}' is not a valid stack output reference")
    val stack = findGradleStack(v.refs.head)
    val stackName = stack map { s => s.qualifiedName } getOrElse v.refs.head
    val stackRegion = stack map { s => region(s) }
    val outputKey = v.refs(2).capitalize
    (stackName, outputKey, stackRegion)
  }

  private def commandLineParams(stack: GradleStack): Map[ParamKey, ParamValue] = {
    val overrides = projectProperties.filterKeys(_.startsWith(s"stack.${stack.name}"))
    overrides.foldLeft(Map[ParamKey, ParamValue]()) { (z, kv) =>
      val splitted = kv._1.split(".")
      val pk = splitted.length match {
        case 3 => ParamKey(splitted(2))
        case 4 => ParamKey(splitted(3), Some(splitted(2)))
        case _ => raiseUsageError(s"Invalid stack property '${kv._1}'")
      }
      z + (pk -> SimpleParamValue(kv._2))
    }
  }

  private def region(stack: GradleStack): Region = stack.region match {
    case null => pluginOptions.region match {
      case Some(x) => x
      case _       => raiseBuildScriptError(s"No region could be found for stack '${stack.name}'")
    }
    case r => r
  }

  private def byEnvironment[V](m: Map[ParamKey, V], env: Option[Environment]): Map[String, String] = {
    def convert(m: Map[ParamKey, V]) = m map { case (k, v) => (k.property, v.toString) }
    convert(m filterKeys { k => k.environment.isEmpty }) ++ convert(m filterKeys { k => k.environment == env })
  }

  private def log(stack: Stack) = {
    def printMap(m: Map[String, String]) = m.foldLeft("") { (z, a) => s"${z}\n    ${a._1}: ${a._2}" }
    logger.lifecycle(
    s"""Resolved properties for stack '${stack.name}':
       |  Qualified name: ${stack.qualifiedName}
       |  Region: ${stack.region}
       |  Tags: ${printMap(stack.tags)}
       |  Properties: ${printMap(stack.params)}""".stripMargin)
  }

  private def targetGradleStack: GradleStack = {
    val stackName = projectProperties.getOrElse("stack.name", raiseUsageError("Parameter stack.name not specified"))
    findGradleStack(stackName).getOrElse(raiseUsageError(s"Stack '${stackName}' is not defined"))
  }

  private def findGradleStack(name: String): Option[GradleStack] =
    Option(gradleStackContainer.findByName(name))

  private def gradleStackContainer: FactoryNamedDomainObjectContainer[GradleStack] =
    getProject.getExtensions.getByName("stacks").asInstanceOf[FactoryNamedDomainObjectContainer[GradleStack]]

  private def targetEnvironment: Option[Environment] =
    projectProperties.get("stack.environment")
}

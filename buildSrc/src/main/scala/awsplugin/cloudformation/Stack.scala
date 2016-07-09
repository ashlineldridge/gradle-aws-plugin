package awsplugin.cloudformation

import java.io.File
import java.util.{Map => JMap}

import groovy.lang.{Closure, GroovyObjectSupport, MissingMethodException}

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MMap}
import Stack._
import com.amazonaws.regions.{Region, Regions}

object Stack {
  type Environment = String
}

case class Stack(name: String) extends GroovyObjectSupport {

  var template: File = null

  private[cloudformation] var qualifiedName: String = name
  private[cloudformation] var region: Region = null
  private[cloudformation] val tags: MMap[PropertyKey, String] = MMap()
  private[cloudformation] val props: MMap[PropertyKey, PropertyValue] = MMap()
  private[cloudformation] def templateOption = Option(template)

  private var currentEnvironment: Option[Environment] = None

  def setName(name: String) = qualifiedName = name
  def getName() = name
  def setRegion(r: String) = region = Region.getRegion(Regions.fromName(r))
  def setTags(tags: JMap[String, String]) = {
    this.tags.clear()
    addTags(tags)
  }
  def addTags(tags: JMap[String, String]) =
    tags.asScala.foreach(x => this.tags.put(PropertyKey(x._1, currentEnvironment), x._2))

  def props(env: Option[Environment]): Map[String, String] = byEnvironment(props.toMap, env)

  def tags(env: Option[Environment]): Map[String, String] = byEnvironment(tags.toMap, env)

  def methodMissing(m: String, arg: Any): Any = {
    val args = arg.asInstanceOf[Array[Object]]
    if (args.length > 0 && args(0).isInstanceOf[Closure[_]]) {
      currentEnvironment = Some(m)
      val c = args(0).asInstanceOf[Closure[_]]
      c.call()
      currentEnvironment = None
    } else {
      throw new MissingMethodException(m, getClass(), args)
    }
  }

  def propertyMissing(p: String): Any = {
    ReferencePropertyValue(List(p))
  }

  def propertyMissing(p: String, v: Any): Any = {
    val pk = PropertyKey(p, currentEnvironment)
    val pv = v match {
      case x: PropertyValue => x
      case y: String => SimplePropertyValue(y)
    }
    props.put(pk, pv)
  }

  private def byEnvironment[V](m: Map[PropertyKey, V], env: Option[Environment]): Map[String, String] = {
    def convert(m: Map[PropertyKey, V]) = m map { case (k, v) => (k.property, v.toString) }
    convert(m filterKeys { k => k.environment.isEmpty }) ++ convert(m filterKeys { k => k.environment == env })
  }
}


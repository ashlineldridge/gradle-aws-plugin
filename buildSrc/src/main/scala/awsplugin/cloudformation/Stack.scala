package awsplugin.cloudformation

import java.io.File
import java.util.{Map => JMap}

import groovy.lang.{Closure, GroovyObjectSupport, MissingMethodException}

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MMap}

import Stack._

object Stack {
  type Environment = String
}

class Stack(name: String) extends GroovyObjectSupport {

  var template: File = null

  private[cloudformation] var qualifiedName: String = name
  private[cloudformation] var region: Option[String] = None
  private[cloudformation] val tags: MMap[PropertyKey, String] = MMap()
  private[cloudformation] val props: MMap[PropertyKey, PropertyValue] = MMap()
  private[cloudformation] def templateOption = Option(template)

  private var currentEnvironment: Option[Environment] = None

  def setName(name: String) = qualifiedName = name
  def getName() = name
  def setRegion(r: String) = region = Some(r)
  def setTags(tags: JMap[String, String]) = {
    this.tags.clear()
    addTags(tags)
  }
  def addTags(tags: JMap[String, String]) =
    tags.asScala.foreach(x => this.tags.put(PropertyKey(x._1, currentEnvironment), x._2))

  def methodMissing(m: String, arg: Any): Any = {
    println(s"You called methodMissing with ${m} and ${arg.getClass.getName}")
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
    println(s"You called propertyMissing with ${p}")
    ReferencePropertyValue(List(p))
  }

  def propertyMissing(p: String, v: Any): Any = {
    println(s"You called propertyMissing with ${p} and ${v}")
    val pk = PropertyKey(p, currentEnvironment)
    val pv = v match {
      case x: PropertyValue => x
      case y: String => SimplePropertyValue(y)
    }
    props.put(pk, pv)
  }
}


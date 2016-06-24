package awsplugin.cloudformation

import java.io.File
import java.util.{Map => JMap}

import groovy.lang.{Closure, GroovyObjectSupport, MissingMethodException}

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MMap}

class Stack(name: String) extends GroovyObjectSupport {

  var template: File = null

  private[cloudformation] var qualifiedName: String = name
  private[cloudformation] val tags: MMap[PropertyKey, String] = MMap()
  private[cloudformation] def templateOption = Option(template)

  private var currentEnvironment: Option[String] = None

  def setName(name: String) = qualifiedName = name
  def getName() = name
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
      null
    } else {
      throw new MissingMethodException(m, getClass(), args)
    }
  }

  def propertyMissing(p: String): Any = {
    println(s"You called propertyMissing with ${p}")
    p
  }

  def propertyMissing(p: String, v: Any): Any = {
    println(s"You called propertyMissing with ${p} and ${v}")
    v
  }
}


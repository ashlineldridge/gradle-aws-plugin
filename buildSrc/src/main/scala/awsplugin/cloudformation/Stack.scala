package awsplugin.cloudformation

import java.io.File
import java.util.{Map => JMap}

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MMap}

class Stack(name: String) {

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

  def methodMissing(m: String, arg: Object): Object = {
    println(s"You called methodMissing with ${m} and ${arg.getClass.getName}")
    this
  }

  def propertyMissing(p: String): Object = {
    println(s"You called propertyMissing with ${p}")
    p
  }

  def propertyMissing(p: String, v: Object): Object = {
    println(s"You called propertyMissing with ${p} and ${v}")
    v
  }
}


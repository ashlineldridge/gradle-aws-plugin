package awsplugin.cloudformation.dsl

import java.io.File
import java.util.{Map => JMap}

import awsplugin.cloudformation.dsl.GradleStack._
import com.amazonaws.regions.{Region, Regions}
import groovy.lang.{Closure, GroovyObjectSupport, MissingMethodException}

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MMap}

object GradleStack {
  type Environment = String

  case class ParamKey(property: String, environment: Option[Environment] = None)

  trait ParamValue {
    def value: String
  }

  case class SimpleParamValue(v: String) extends ParamValue {
    override def value: String = v
    override def toString: String = v
  }

  case class ReferenceParamValue(refs: List[String]) extends ParamValue {
    def propertyMissing(p: String) = ReferenceParamValue(refs :+ p)
    override def value: String = refs.mkString(".")
    override def toString: String = value
  }
}

case class GradleStack(name: String) extends GroovyObjectSupport {

  var template: File = null

  private[cloudformation] var qualifiedName: String = name
  private[cloudformation] var region: Region = null
  private[cloudformation] val tags: MMap[ParamKey, String] = MMap()
  private[cloudformation] val params: MMap[ParamKey, ParamValue] = MMap()
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
    tags.asScala.foreach(x => this.tags.put(ParamKey(x._1, currentEnvironment), x._2))

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
    ReferenceParamValue(List(p))
  }

  def propertyMissing(p: String, v: Any): Any = {
    val pk = ParamKey(p, currentEnvironment)
    val pv = v match {
      case x: ParamValue => x
      case y: String => SimpleParamValue(y)
    }
    params.put(pk, pv)
  }
}


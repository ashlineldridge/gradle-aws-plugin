package awsplugin.cloudformation

import awsplugin.cloudformation.Stack.Environment

case class PropertyKey(property: String, environment: Option[Environment] = None)

trait PropertyValue {
  def value: String
}

case class SimplePropertyValue(v: String) extends PropertyValue {
  override def value: String = v
  override def toString: String = v
}

case class ReferencePropertyValue(refs: List[String]) extends PropertyValue {
  def propertyMissing(p: String) = ReferencePropertyValue(refs :+ p)
  override def value: String = refs.mkString(".")
  override def toString: String = value
}


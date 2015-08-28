package com.itszuvalex.femtocraft.core.Nanites.Attribute

import scala.collection.mutable

/**
 * Created by Christopher on 7/31/2015.
 */
object AttributeRegistry {
  private val attributeMap = mutable.HashMap[String, INaniteAttribute]()

  def getAttribute(attribute: String) = attributeMap.get(attribute)

  def addAttribute(attribute: INaniteAttribute) = attributeMap.put(attribute.getName, attribute)

}

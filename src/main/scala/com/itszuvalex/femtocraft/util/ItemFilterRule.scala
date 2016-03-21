package com.itszuvalex.femtocraft.util

import scala.collection.mutable

/**
  * Created by Christopher Harris (Itszuvalex) on 2/1/2016.
  */
object ItemFilterRule {
  val typeToClass = new mutable.HashMap[String, Class[_ <: IItemFilterRule]]()

  def registerFilterType(name: String, clazz: Class[_ <: IItemFilterRule]) = typeToClass(name) = clazz

  def getFilterClass(name: String) = typeToClass.get(name)

}

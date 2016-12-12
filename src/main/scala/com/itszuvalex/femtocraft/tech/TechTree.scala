package com.itszuvalex.femtocraft.tech

import scala.collection.mutable.ArrayBuffer

class TechTree(val name: String) {
  var techs: ArrayBuffer[TechBase] = ArrayBuffer[TechBase]()

  def getTech(tName: String): TechBase = {
    techs.foreach(t => if(t.name.equals(tName)) return t)
    null
  }
}

package com.itszuvalex.femtocraft.tech

class TechBase(val name: String, val parent: TechBase, val cost: Int) {
  if(parent != null) {
    parent.addChild(this)
  }

  private val children = new Array[TechBase](2)

  def addChild(techBase: TechBase): Boolean = {
    if (children(1) != null) {
      false
    } else if(children(0) == null) {
      children(0) = techBase
      true
    } else {
      children(1) = techBase
      true
    }
  }
}

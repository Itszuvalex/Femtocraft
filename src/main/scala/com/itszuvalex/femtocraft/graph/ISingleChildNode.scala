package com.itszuvalex.femtocraft.graph

/**
  * Created by Christopher Harris (Itszuvalex) on 12/17/15.
  */
trait ISingleChildNode[CType] {
  def getChild: CType

  def canSetChild(loc: CType): Boolean

  def setChild(loc: CType): Boolean
}

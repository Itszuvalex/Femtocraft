package com.itszuvalex.femtocraft.graph

/**
  * Created by Christopher Harris (Itszuvalex) on 12/17/15.
  */
trait ISingleParentNode[PType] {
  def getParent: PType

  def canSetParent(loc: PType): Boolean

  def setParent(loc: PType): Boolean
}

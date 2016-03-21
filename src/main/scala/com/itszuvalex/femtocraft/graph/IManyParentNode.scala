package com.itszuvalex.femtocraft.graph

/**
  * Created by Christopher Harris (Itszuvalex) on 12/17/15.
  */
trait IManyParentNode[PType] {
  def getParents: scala.collection.Set[PType]

  def canAddParent(loc: PType): Boolean

  def addParent(loc: PType): Boolean

  def removeParent(loc: PType): Boolean
}

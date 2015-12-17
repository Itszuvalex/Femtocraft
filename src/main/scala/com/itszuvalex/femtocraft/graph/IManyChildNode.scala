package com.itszuvalex.femtocraft.graph

/**
  * Created by Christopher Harris (Itszuvalex) on 12/17/15.
  */
trait IManyChildNode[CType] {
  def getChildren: scala.collection.Set[CType]

  def canAddChild(loc: CType): Boolean

  def addChild(loc: CType): Boolean

  def removeChild(loc: CType): Boolean
}

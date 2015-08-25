package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.itszulib.api.core.Loc4

/**
 * Created by Christopher Harris (Itszuvalex) on 8/24/15.
 */
trait INaniteHive {

  def getType: String

  def getHiveLoc: Loc4

  def getNodeLocs: scala.collection.Set[Loc4]

  def getNodes: Iterable[INaniteNode]

  def addNode(node: INaniteNode): Boolean

  def canAddNode(node: INaniteNode): Boolean

  def removeNode(node: INaniteNode)

  def connectionRadius: Float

}

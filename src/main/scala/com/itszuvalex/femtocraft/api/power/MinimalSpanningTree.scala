package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.logistics.TileNetwork

import scala.collection.mutable

/**
  * Created by Chris on 1/2/2017.
  */
object MinimalSpanningTree {

  def calculate(network: TileNetwork[_, _]): scala.collection.Map[Loc4, scala.collection.Set[Loc4]] = {
    if (network.size <= 1) return Map()
    if (network.nodeMap.keySet.diff(network.connectionMap.keySet).nonEmpty) return Map()

    val viewedNodes = new mutable.HashSet[Loc4]()
    val ordering = new Ordering[(Loc4, Loc4)] {
      override def compare(x: (Loc4, Loc4), y: (Loc4, Loc4)): Int = {
        val leftDist = x._1.distSqr(x._2)
        val rightDist = y._1.distSqr(y._2)

        leftDist.compare(rightDist) match {
          case 0 =>
            x._1.compareTo(y._1) match {
              case 0 =>
                x._2.compareTo(y._2)
              case a => a
            }
          case a => a
        }
      }
    }
    val edgeList = new mutable.TreeSet[(Loc4, Loc4)]()(ordering)
    val retSet = new mutable.HashMap[Loc4, mutable.HashSet[Loc4]]()

    var node = network.nodeMap.head._1
    while (viewedNodes.size < network.size) {
      val connections = network.getConnections(node).get // No else.  We want to crash if we have a malformed network - we should have a connection for every node.
      connections.foreach(loc => edgeList += ((node, loc)))

      val edge = edgeList.find(a =>
        !(viewedNodes.contains(a._1) && viewedNodes.contains(a._2))
      ).get

      retSet.getOrElseUpdate(edge._1, new mutable.HashSet[Loc4]()) += edge._2
      retSet.getOrElseUpdate(edge._2, new mutable.HashSet[Loc4]()) += edge._1

      viewedNodes += node
      if (node.compareTo(edge._1) == 0)
        node = edge._2
      else
        node = edge._1
    }

    retSet
  }

}

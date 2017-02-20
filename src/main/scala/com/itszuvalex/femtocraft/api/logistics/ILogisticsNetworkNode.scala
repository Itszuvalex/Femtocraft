package com.itszuvalex.femtocraft.api.logistics

import java.util

import com.itszuvalex.itszulib.logistics.TileNetworkNode
import net.minecraft.util.EnumFacing

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

/**
  * Created by Chris on 2/19/2017.
  */
trait ILogisticsNetworkNode extends TileNetworkNode[ILogisticsNetworkNode, LogisticsNetwork] {
  def getConnections(facing: EnumFacing): util.Map[IResource, util.Collection[IConnection]]

  def getConnectionsForResource(facing: EnumFacing, resource: IResource): util.Collection[IConnection] =
    getConnections(facing).find(_._1 == resource).map(_._2).getOrElse(new ArrayBuffer[IConnection]())

  def getConnectionsForResourceForChannel(facing: EnumFacing, resource: IResource, channel: String): util.Collection[IConnection]
  = getConnectionsForResource(facing, resource).filter(_.channel == channel)

}

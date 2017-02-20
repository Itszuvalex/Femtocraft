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
  def getConnections(facing: EnumFacing): util.Map[IResource[_], util.Collection[IConnection[_]]]

  def getConnectionsForResource[T](facing: EnumFacing, resource: IResource[T]): util.Collection[IConnection[T]] =
    getConnections(facing).find(_._1 == resource).map(_._2.asInstanceOf[util.Collection[IConnection[T]]]).getOrElse(new ArrayBuffer[IConnection[T]]())

  def getConnectionsForResourceForChannel[T](facing: EnumFacing, resource: IResource[T], channel: String): util.Collection[IConnection[T]]
  = getConnectionsForResource(facing, resource).filter(_.channel == channel)

}

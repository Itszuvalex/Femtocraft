package com.itszuvalex.femtocraft.api.logistics

import java.util

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.logistics.{IPersistedConnectableNetworkNode, TileNetworkNode}
import net.minecraft.util.EnumFacing

import scala.collection.JavaConversions._

/**
  * Created by Chris on 2/19/2017.
  */
trait ILogisticsNetworkNode extends IPersistedConnectableNetworkNode[ILogisticsNetworkNode, LogisticsNetwork] {
  def getConnections[T](facing: EnumFacing): util.Collection[IConnection[T]]

  def getConnectionsForResourceForChannel[T](facing: EnumFacing, resource: IResource[T], channel: String): util.Collection[IConnection[T]]
  = getConnectionsForResource(facing, resource).filter(_.channel == channel)

  def getConnectionsForResource[T](facing: EnumFacing, resource: IResource[T]): util.Collection[IConnection[T]] =
    getConnections[T](facing).filter(_.resource == resource)
}

package com.itszuvalex.femtocraft.api.logistics

import java.util

import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.util.EnumFacing

/**
  * Created by Chris on 2/21/2017.
  */
trait IConnectionProvider {

  /**
    * Given loc and facing to allow the provider to build the connection...connections...as needed
    *
    * @param loc    Loc holding this connection provider
    * @param facing Facing
    *
    * @return Set of Connections provided by this provider
    */
  def getConnections[T](loc: Loc4, facing: EnumFacing): util.Collection[IConnection[T]]

  def addTooltip(tooltip: util.List[String]): Unit
}

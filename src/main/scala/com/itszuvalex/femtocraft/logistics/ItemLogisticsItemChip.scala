package com.itszuvalex.femtocraft.logistics

import java.util

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.logistics.{ConnectionDirection, IConnection, IConnectionProvider}
import com.itszuvalex.femtocraft.logistics.connections.ItemConnection
import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}

import scala.collection.JavaConversions._

/**
  * Created by Chris on 2/23/2017.
  */
class ItemLogisticsItemChip extends Item {
  override def initCapabilities(stack: ItemStack, nbt: NBTTagCompound): ICapabilityProvider = {
    new ICapabilityProvider {
      override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = if (capability == Capabilities.ITEM_CONNECTION_PROVIDER) {
        new IConnectionProvider {
          /**
            * Given loc and facing to allow the provider to build the connection...connections...as needed
            *
            * @param loc    Loc holding this connection provider
            * @param facing Facing
            *
            * @return Set of Connections provided by this provider
            */
          override def getConnections[V](loc: Loc4, facing: EnumFacing): util.Collection[IConnection[V]] = {
            if (!stack.hasTagCompound) {
              stack.setTagCompound(new NBTTagCompound)
            }
            val inbt = stack.getTagCompound
            val con = new ItemConnection(loc, facing, inbt, 5000d, 1, 16).asInstanceOf[IConnection[V]]
            con.setDirection(if (facing.getIndex % 2 == 0) ConnectionDirection.INPUT else ConnectionDirection.OUTPUT)
            Set(con)
          }

          override def addTooltip(tooltip: util.List[String]): Unit = {}
        }.asInstanceOf[T]
      } else null.asInstanceOf[T]

      override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == Capabilities.ITEM_CONNECTION_PROVIDER
    }
  }
}

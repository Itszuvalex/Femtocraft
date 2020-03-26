package com.itszuvalex.femtocraft.logistics.item

import java.util

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.logistics.{ConnectionDirection, IConnection, IConnectionProvider}
import com.itszuvalex.femtocraft.logistics.connections.ItemConnection
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.util.ChatHelper
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}

import scala.collection.JavaConversions._

/**
  * Created by Chris on 2/23/2017.
  */
class ItemLogisticsItemChip extends Item {

  //  override def isDamageable: Boolean = true

  override def isDamaged(stack: ItemStack): Boolean = {
    getDamage(stack) != getMaxDamage(stack)
  }

  override def getMaxDamage(stack: ItemStack): Int = {
    var sum = 0d
    stack.getCapability(Capabilities.ITEM_CONNECTION_PROVIDER, null).getConnections[Any](new Loc4, null).foreach(a => sum += a.flopsMaximum)
    sum.toInt
  }

  override def getDamage(stack: ItemStack): Int = {
    var sum = 0d
    stack.getCapability(Capabilities.ITEM_CONNECTION_PROVIDER, null).getConnections[Any](new Loc4, null).foreach(a => sum += a.flopsRemaining)
    getMaxDamage(stack) - sum.toInt
  }

  override def initCapabilities(stack: ItemStack, nbt: NBTTagCompound): ICapabilityProvider = {
    new ICapabilityProvider {
      override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = if (capability == Capabilities.ITEM_CONNECTION_PROVIDER) {
        new IConnectionProvider {
          /**
            * Given loc and facing to allow the provider to build the connection...connections...as needed
            *
            * @param loc    Loc holding this connection provider
            * @param facing Facing
            * @return Set of Connections provided by this provider
            */
          override def getConnections[V](loc: Loc4, facing: EnumFacing): util.Collection[IConnection[V]] = {
            var created = false
            if (!stack.hasTagCompound) {
              stack.setTagCompound(new NBTTagCompound)
              created = true
            }
            val inbt = stack.getTagCompound
            val con  = new ItemConnection(loc, facing, inbt, 5000d, 1, 16)
            if (created && facing != null) {
              con.setDirection(if (facing.getIndex % 2 == 0) ConnectionDirection.INPUT else ConnectionDirection.OUTPUT)
              con.interfaceDirection = facing.getOpposite
            }
            Set(con.asInstanceOf[IConnection[V]])
          }

          override def addTooltip(tooltip: util.List[String]): Unit = {
            val con       = new ItemConnection(new Loc4, null, Option(stack.getTagCompound).getOrElse(new NBTTagCompound), 5000d, 1, 16)
            val itemstack = con.ibuffer

            tooltip += f"${ChatHelper.yellow("Item: ")}${if (itemstack.isEmpty) ChatHelper.italic("Empty") else itemstack.toMinecraft.toString}"
            tooltip += f"${ChatHelper.yellow("FLOPs: ")}${con.flopsRemaining}%,.1f/${con.flopsMaximum}%,.1f"
            tooltip += f"${ChatHelper.yellow("Passive FLOPs: ")}${con.passiveFlopGen}%,.1f"
            tooltip += f"${ChatHelper.yellow("Channel: ")}${con.channel}"
            tooltip += f"${ChatHelper.yellow("Items Per Op: ")}${con.itemsPerOp}"
            tooltip += f"${ChatHelper.yellow("Buffer Size: ")}${con.stackLimit}"
            tooltip += f"${ChatHelper.yellow("Mode: ")}${con.direction}"
            tooltip += f"${ChatHelper.yellow("Interface Direction: ")}${con.interfaceDirection.toString}"
          }
        }.asInstanceOf[T]
      } else null.asInstanceOf[T]

      override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == Capabilities.ITEM_CONNECTION_PROVIDER
    }
  }

  override def addInformation(stack: ItemStack, worldIn: World, tooltip: util.List[String], flagIn: ITooltipFlag) = {
    super.addInformation(stack, worldIn, tooltip, flagIn)
    stack.getCapability(Capabilities.ITEM_CONNECTION_PROVIDER, null).addTooltip(tooltip)
  }
}

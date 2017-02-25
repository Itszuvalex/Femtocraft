package com.itszuvalex.femtocraft.logistics.item

import java.util

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.logistics.{ConnectionDirection, IConnection, IConnectionProvider}
import com.itszuvalex.femtocraft.logistics.connections.ItemConnection
import com.itszuvalex.itszulib.api.core.Loc4
import com.mojang.realmsclient.gui.ChatFormatting
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}

import scala.collection.JavaConversions._

/**
  * Created by Chris on 2/23/2017.
  */
class ItemLogisticsItemChip extends Item {

  override def isDamageable: Boolean = true

  override def isDamaged(stack: ItemStack): Boolean = {
    getDamage(stack) != getMaxDamage(stack)
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
            *
            * @return Set of Connections provided by this provider
            */
          override def getConnections[V](loc: Loc4, facing: EnumFacing): util.Collection[IConnection[V]] = {
            if (!stack.hasTagCompound) {
              stack.setTagCompound(new NBTTagCompound)
            }
            val inbt = stack.getTagCompound
            val con = new ItemConnection(loc, facing, inbt, 5000d, 1, 16).asInstanceOf[IConnection[V]]
            if (facing != null)
              con.setDirection(if (facing.getIndex % 2 == 0) ConnectionDirection.INPUT else ConnectionDirection.OUTPUT)
            Set(con)
          }

          override def addTooltip(tooltip: util.List[String]): Unit = {
            val con = new ItemConnection(new Loc4, null, Option(stack.getTagCompound).getOrElse(new NBTTagCompound), 5000d, 1, 16)
            val itemstack = con.ibuffer
            tooltip += ChatFormatting.YELLOW + "Item: " + ChatFormatting.RESET + (if (itemstack.isEmpty) ChatFormatting.ITALIC + "Empty" else itemstack.toMinecraft.toString) + ChatFormatting.RESET
            tooltip += ChatFormatting.YELLOW + "FLOPs: " + ChatFormatting.RESET + con.flopsRemaining + "/" + con.flopsMaximum
            tooltip += ChatFormatting.YELLOW + "Channel: " + ChatFormatting.RESET + con.channel
            tooltip += ChatFormatting.YELLOW + "Items Per Op: " + ChatFormatting.RESET + con.itemsPerOp
            tooltip += ChatFormatting.YELLOW + "Buffer Size: " + ChatFormatting.RESET + con.stackLimit
            tooltip += ChatFormatting.YELLOW + "Mode: " + ChatFormatting.RESET + con.direction
            tooltip += ChatFormatting.YELLOW + "Interface Direction: " + ChatFormatting.RESET + con.interfaceDirection.toString
          }
        }.asInstanceOf[T]
      } else null.asInstanceOf[T]

      override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == Capabilities.ITEM_CONNECTION_PROVIDER
    }
  }

  override def getMaxDamage(stack: ItemStack): Int = {
    var sum = 0d
    stack.getCapability(Capabilities.ITEM_CONNECTION_PROVIDER, null).getConnections[Any](new Loc4, null).foreach(a => sum += a.flopsMaximum)
    sum.toInt
  }

  override def getDamage(stack: ItemStack): Int = {
    var sum = 0d
    stack.getCapability(Capabilities.ITEM_CONNECTION_PROVIDER, null).getConnections[Any](new Loc4, null).foreach(a => sum += a.flopsRemaining)
    sum.toInt
  }

  override def addInformation(stack: ItemStack, playerIn: EntityPlayer, tooltip: util.List[String], advanced: Boolean): Unit = {
    super.addInformation(stack, playerIn, tooltip, advanced)
    stack.getCapability(Capabilities.ITEM_CONNECTION_PROVIDER, null).addTooltip(tooltip)
  }
}

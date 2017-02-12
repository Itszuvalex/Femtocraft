package com.itszuvalex.femtocraft.industry.item

import com.itszuvalex.femtocraft.api.Capabilities
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * Created by Chris on 12/11/2016.
  */
object ItemMultiTool {
  val TOOL_PICKAXE = "pickaxe"
  val TOOL_AXE     = "axe"
  val TOOL_SHOVEL  = "shovel"
}

class ItemMultiTool extends Item {
  override def getItemEnchantability: Int = 0

  override def getIsRepairable(toRepair: ItemStack, repair: ItemStack): Boolean = false

  @SideOnly(Side.CLIENT)
  override def isFull3D: Boolean = true

  override def initCapabilities(stack: ItemStack, nbt: NBTTagCompound): ICapabilityProvider = {
    new ICapabilityProvider {
      override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = (if (capability == Capabilities.ITEM_MULTITOOL) new Multitool(nbt) else null).asInstanceOf[T]

      override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == Capabilities.ITEM_MULTITOOL
    }
  }
}

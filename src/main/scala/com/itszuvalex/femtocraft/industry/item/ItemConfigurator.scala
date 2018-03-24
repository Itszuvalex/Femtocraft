package com.itszuvalex.femtocraft.industry.item

import com.itszuvalex.femtocraft.api.{Capabilities, IOverlayRenderItem, OverlayRenderSwitch}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}


object ItemConfigurator {

}

class ItemConfigurator extends Item {
  @SideOnly(Side.CLIENT)
  override def isFull3D: Boolean = true

  override def initCapabilities(stack: ItemStack, nbt: NBTTagCompound): ICapabilityProvider = {
    new ICapabilityProvider {
      override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = if (capability == Capabilities.ITEM_OVERLAY_RENDER) {
        {
          new IOverlayRenderItem {
            override def shouldRender(overlay: OverlayRenderSwitch): Boolean = overlay == OverlayRenderSwitch.ITEM
          }
        }.asInstanceOf[T]
      }
      else null.asInstanceOf[T]

      override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == Capabilities.ITEM_OVERLAY_RENDER
    }
  }
}

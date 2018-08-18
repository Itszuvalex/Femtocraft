package com.itszuvalex.femtocraft.logistics.item

import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.api.storage.ItemStorageNBT
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{ActionResult, EnumActionResult, EnumFacing, EnumHand}
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}

object ItemNanoPack {
  val SIZE = 18
}

class ItemNanoPack extends Item {
  override def onItemRightClick(worldIn: World, playerIn: EntityPlayer, hand: EnumHand): ActionResult[ItemStack] = {
      playerIn.openGui(Femtocraft, GuiIDs.ItemNanoPackID, worldIn, 0, 0, 0)
      val itemStack = playerIn.getHeldItem(hand)
      new ActionResult(EnumActionResult.SUCCESS, itemStack)
  }

  override def initCapabilities(stack: ItemStack, nbt: NBTTagCompound): ICapabilityProvider =
    new ICapabilityProvider {
      override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = if (capability == ItszuLibCapabilities.ITEM_STORAGE) {
        if (!stack.hasTagCompound)
          stack.setTagCompound(new NBTTagCompound)
        new ItemStorageNBT(stack.getTagCompound, ItemNanoPack.SIZE).asInstanceOf[T]
      } else null.asInstanceOf[T]

      override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == ItszuLibCapabilities.ITEM_STORAGE
    }
}

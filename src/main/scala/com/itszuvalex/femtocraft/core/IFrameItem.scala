package com.itszuvalex.femtocraft.core

import com.itszuvalex.itszulib.api.IPreviewable
import net.minecraft.item.{Item, ItemStack}

/**
 * Created by Christopher on 8/26/2015.
 */
trait IFrameItem extends Item with IPreviewable {

  def getFrameType(stack: ItemStack): String

  def getSelectedMultiblock(stack: ItemStack): String

  def setSelectedMultiblock(stack: ItemStack, multi: String)

}

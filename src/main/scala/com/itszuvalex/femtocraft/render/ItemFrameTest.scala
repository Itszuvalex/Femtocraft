package com.itszuvalex.femtocraft.render

import com.itszuvalex.femtocraft.core.IFrameItem
import net.minecraft.item.{Item, ItemStack}

/**
 * Created by Christopher Harris (Itszuvalex) on 8/30/15.
 */
class ItemFrameTest extends Item with IFrameItem {
  override def getFrameType(stack: ItemStack) = "Basic"

  override def getSelectedMultiblock(stack: ItemStack) = "Arc Furnace"

  override def renderID: Int = RenderIDs.framePreviewableID
}

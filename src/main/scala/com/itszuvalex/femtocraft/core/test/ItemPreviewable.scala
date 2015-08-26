package com.itszuvalex.femtocraft.core.test

import com.itszuvalex.femtocraft.core.IPreviewable
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World

/**
 * Created by Christopher Harris (Itszuvalex) on 8/26/15.
 */
class ItemPreviewable extends Item with IPreviewable {
  override def canPlaceAtLocation(stack: ItemStack, world: World, x: Int, y: Int, z: Int): Boolean = true

  /**
   *
   * @return The ID of IPreviewableRenderer.  This is separate from Forge RenderIDs.
   */
  override def renderID: Int = PreviewableIDs.testID
}

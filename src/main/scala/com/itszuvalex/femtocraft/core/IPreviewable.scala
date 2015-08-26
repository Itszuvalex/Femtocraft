package com.itszuvalex.femtocraft.core

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World

/**
 * Created by Christopher Harris (Itszuvalex) on 8/26/15.
 */
trait IPreviewable extends Item {

  def canPlaceAtLocation(stack: ItemStack, world: World, x: Int, y: Int, z: Int): Boolean

  /**
   *
   * @return The ID of IPreviewableRenderer.  This is separate from Forge RenderIDs.
   */
  @SideOnly(Side.CLIENT)
  def renderID: Int

}

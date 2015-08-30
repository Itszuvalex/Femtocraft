package com.itszuvalex.femtocraft.core

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraft.world.World

/**
 * Created by Christopher on 8/26/2015.
 */
@SideOnly(Side.CLIENT)
trait IFrameMultiblockRenderer {
  /**
   * Coordinates are the location to render at.  This is usually the facing off-set location that, if the player right-clicked, a block would be placed at.
   *
   * @param stack ItemStack of IPreviewable Item
   * @param world World
   * @param x X Location
   * @param y Y Location
   * @param z Z Location
   * @param rx X Render location
   * @param ry Y Render location
   * @param rz Z Render location
   */
  def previewRenderAtWorldLocation(stack: ItemStack, world: World, x: Int, y: Int, z: Int,
                                   rx: Double, ry: Double, rz: Double): Unit

  /**
   * Coordinates to render at.  This is for things like generic menu rendering, etc.
   *
   * @param rx
   * @param ry
   * @param rz
   */
  def renderAtLocation(rx: Double, ry: Double, rz: Double): Unit

  /**
   * Render using information contained in stack.
   *
   * @param stack
   * @param rx
   * @param ry
   * @param rz
   */
  def renderAsItem(stack: ItemStack, rx: Double, ry: Double, rz: Double): Unit
}

package com.itszuvalex.femtocraft.core

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.client.model.obj.WavefrontObject

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
   * In-Progress model of the multiblock.
   */
  val previewModel: WavefrontObject

  /**
   * In-Progress model texture of the multiblock.
   */
  val previewTexture: ResourceLocation

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

  /**
   *
   * @return Bounding box for rendering.  (X, Y, Z) (Length, Height, Width)
   */
  def boundingBox: (Int, Int, Int)
}

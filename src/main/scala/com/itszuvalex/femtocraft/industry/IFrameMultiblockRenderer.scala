package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.industry.tile.TileFrame
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
    * Render function for machine in-progress rendering.
    *
    * @param x xPos to render at
    * @param y yPos to render at
    * @param z zPos to render at
    * @param partialTime Partial tick time
    * @param frame Controller TileFrame of the machine.
    *              Store any data that should persist between render calls in `frame.inProgressData`.
    *              If there is a float named `targetTime` in there, after reaching 100% progress it will wait for that point in time to pass before it replaces the frame with the machine.
    */
  def renderInProgressAt(x: Double, y: Double, z: Double, partialTime: Float, frame: TileFrame): Unit

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

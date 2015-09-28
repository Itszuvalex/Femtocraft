package com.itszuvalex.femtocraft.render

import com.itszuvalex.femtocraft.core.Industry.tile.TileFrame
import com.itszuvalex.femtocraft.core.Industry.{IFrameMultiblock, IFrameMultiblockRenderer}
import com.itszuvalex.itszulib.render.RenderUtils
import net.minecraft.client.renderer.Tessellator
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import org.lwjgl.opengl.GL11

/**
 * Created by Christopher Harris (Itszuvalex) on 8/30/15.
 */
class GenericFrameMultiblockRenderer extends IFrameMultiblockRenderer {
  var multi: IFrameMultiblock = null

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
  override def previewRenderAtWorldLocation(stack: ItemStack, world: World, x: Int, y: Int, z: Int, rx: Double, ry: Double, rz: Double): Unit = {
    Tessellator.instance.startDrawingQuads()
    GL11.glDisable(GL11.GL_CULL_FACE)
    GL11.glEnable(GL11.GL_BLEND)
    if (multi.canPlaceAtLocation(world, x, y, z)) {
      Tessellator.instance.setColorRGBA_F(0, 1, 0, .5f)
    }
    else {
      Tessellator.instance.setColorRGBA_F(1, 0, 0, .5f)
    }
    multi.getTakenLocations(world, x, y, z).toList.sortWith { case (a1, a2) =>
      a1.distSqr((rx + x).toInt,
                 (ry + y).toInt,
                 (rz + z).toInt) <
      a2.distSqr((rx + x).toInt,
                 (ry + y).toInt,
                 (rz + z).toInt)
                                                            }
    .foreach { loc =>
      RenderUtils.renderCube(rx.toFloat + (loc.x - x), ry.toFloat + (loc.y - y), rz.toFloat + (loc.z - z), 0, 0, 0, 1, 1, 1, Blocks.iron_block.getIcon(0, 0))
             }
    Tessellator.instance.draw()
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glDisable(GL11.GL_BLEND)
  }


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
  override def renderInProgressAt(x: Double, y: Double, z: Double, partialTime: Float, frame: TileFrame): Unit = {
   
  }

  /**
   * Coordinates to render at.  This is for things like generic menu rendering, etc.
   *
   * @param rx
   * @param ry
   * @param rz
   */
  override def renderAtLocation(rx: Double, ry: Double, rz: Double): Unit = {

  }

  /**
   * Render using information contained in stack.
   *
   * @param stack
   * @param rx
   * @param ry
   * @param rz
   */
  override def renderAsItem(stack: ItemStack, rx: Double, ry: Double, rz: Double): Unit = {
  }

  /**
   *
   * @return Bounding box for rendering.  (X, Y, Z) (Length, Height, Width)
   */
  override def boundingBox: (Int, Int, Int) = (2, 2, 2)
}

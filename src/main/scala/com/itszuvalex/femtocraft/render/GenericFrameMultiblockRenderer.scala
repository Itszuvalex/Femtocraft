package com.itszuvalex.femtocraft.render

import com.itszuvalex.femtocraft.industry.tile.TileFrame
import com.itszuvalex.femtocraft.industry.{IFrameMultiblock, IFrameMultiblockRenderer}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.render.RenderUtils
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
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
    * @param loc
    * @param rx    X Render location
    * @param ry    Y Render location
    * @param rz    Z Render location
    */
  override def previewRenderAtWorldLocation(stack: ItemStack, loc: Loc4, rx: Double, ry: Double, rz: Double): Unit = {
    GL11.glDisable(GL11.GL_CULL_FACE)
    GL11.glEnable(GL11.GL_BLEND)
    //                              if (multi.canPlaceAtLocation(world, x, y, z)) {
    //                                Tessellator.instance.setColorRGBA_F(0, 1, 0, .5f)
    //                              }
    //                              else {
    //                                Tessellator.instance.setColorRGBA_F(1, 0, 0, .5f)
    //                              }
    multi.getTakenLocations(loc).toList.sortWith { case (a1, a2) =>
      a1.distSqr((rx + loc.x).toInt,
        (ry + loc.y).toInt,
        (rz + loc.z).toInt) <
        a2.distSqr((rx + loc.x).toInt,
          (ry + loc.y).toInt,
          (rz + loc.z).toInt)
    }
      .foreach { loc =>
        RenderUtils.renderCube(rx.toFloat + (loc.x - loc.x), ry.toFloat + (loc.y - loc.y), rz.toFloat + (loc.z - loc.z), 0, 0, 0, 1, 1, 1, RenderUtils.getDefaultTextureForBlock(Blocks.IRON_BLOCK))
      }
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glDisable(GL11.GL_BLEND)
  }


  /**
    * Render function for machine in-progress rendering.
    *
    * @param x           xPos to render at
    * @param y           yPos to render at
    * @param z           zPos to render at
    * @param partialTime Partial tick time
    * @param frame       Controller TileFrame of the machine.
    *                    Store any data that should persist between render calls in `frame.inProgressData`.
    *                    If there is a float named `targetTime` in there, after reaching 100% progress it will wait for that point in time to pass before it replaces the frame with the machine.
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

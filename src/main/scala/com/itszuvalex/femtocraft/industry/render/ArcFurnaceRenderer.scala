package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.core.IFrameMultiblockRenderer
import com.itszuvalex.femtocraft.core.Industry.tile.TileFrame
import com.itszuvalex.femtocraft.industry.tile.TileArcFurnace
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.obj.WavefrontObject
import org.lwjgl.opengl.GL11

/**
 * Created by Christopher on 9/19/2015.
 */
object ArcFurnaceRenderer {
  val modelLoc   = Resources.Model("arc furnace/Arc Furnace.obj")
  val textureLoc = Resources.Model("arc furnace/Arc Furnace Template.png")
  val inProgressModel = AdvancedModelLoader.loadModel(Resources.Model("_in-progress/arc furnace/Arc Furnace In-Progress.obj")).asInstanceOf[WavefrontObject]
  val inProgressTexLoc = Resources.Model("_in-progress/arc furnace/Arc Furnace In-Progress.png")
}


class ArcFurnaceRenderer extends TileEntitySpecialRenderer with IFrameMultiblockRenderer {
  val model = AdvancedModelLoader.loadModel(ArcFurnaceRenderer.modelLoc)

  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    tile match {
      case furnace: TileArcFurnace if furnace.isController =>
        renderAtLocation(x, y, z)
      case _                                               =>
    }
  }

  override def renderInProgressAt(x: Double, y: Double, z: Double, dx: Double, dy: Double, dz: Double, partialTime: Float, frame: TileFrame): Unit = {

    Minecraft.getMinecraft.getTextureManager.bindTexture(ArcFurnaceRenderer.inProgressTexLoc)

    GL11.glPushMatrix()
    GL11.glTranslated(x + dx, y + dy, z + dz)
    //    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    GL11.glColor4f(1f, 1f, 1f, 1f)

    val timePerPart = frame.totalMachineBuildTime / ArcFurnaceRenderer.inProgressModel.groupObjects.size()
    val currentPart = math.ceil(frame.progress / ArcFurnaceRenderer.inProgressModel.groupObjects.size().toDouble).toInt

    if (currentPart > 1) {
      for (i <- 1 until currentPart) {
        ArcFurnaceRenderer.inProgressModel.renderPart("Stage" + (if (i < 10) "0" else "") + i)
      }
    }
    val time = frame.getWorldObj.getTotalWorldTime + partialTime
    if (currentPart != frame.inProgressData.getOrElseUpdate("lastPart", 0)) {
      frame.inProgressData("targetTime") = time + timePerPart
      frame.inProgressData("lastPart") = currentPart
    }
    GL11.glColor4ub(255.toByte, 255.toByte, 255.toByte, (256 - (256 / math.min(16f, timePerPart)) * math.min(math.min(16f, timePerPart), frame.inProgressData.getOrElseUpdate("targetTime", 0f).asInstanceOf[Float] - time)).toByte)
    ArcFurnaceRenderer.inProgressModel.renderPart("Stage" + (if (currentPart < 10) "0" else "") + currentPart)
    //    GL11.glEnable(GL11.GL_LIGHTING)
    GL11.glPopMatrix()
  }

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
    renderAtLocation(rx, ry, rz)
  }


  /**
   * Coordinates to render at.  This is for things like generic menu rendering, etc.
   *
   * @param rx
   * @param ry
   * @param rz
   */
  override def renderAtLocation(rx: Double, ry: Double, rz: Double): Unit = {
    GL11.glPushMatrix()
    GL11.glTranslated(rx + 1, ry, rz + 1)
    GL11.glColor3f(1, 1, 1)
    Minecraft.getMinecraft.getTextureManager.bindTexture(ArcFurnaceRenderer.textureLoc)
    model.renderAll()
    GL11.glPopMatrix()
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
    renderAtLocation(rx, ry, rz)
  }

  /**
   *
   * @return Bounding box for rendering.  (X, Y, Z) (Length, Height, Width)
   */
  override def boundingBox: (Int, Int, Int) = (2, 3, 2)
}

package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.industry.IFrameMultiblockRenderer
import com.itszuvalex.femtocraft.industry.tile.{TileArcFurnace, TileFrame}
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher on 9/19/2015.
  */
object ArcFurnaceRenderer {
  val modelLoc           = Resources.CustomModelBlock("arc furnace/arc furnace.obj")
  val textureLoc         = Resources.CustomModelBlockTex("arc furnace/arc furnace template.png")
  val inProgressModelLoc = Resources.CustomModelBlock("arc furnace/arc furnace in-progress.obj")
  val inProgressTexLoc   = Resources.CustomModelBlockTex("arc furnace/arc furnace in-progress.png")
}


class ArcFurnaceRenderer extends TileEntitySpecialRenderer[TileArcFurnace] with IFrameMultiblockRenderer {
  val model = LoadObj(ArcFurnaceRenderer.modelLoc)

  override def renderTileEntityAt(te: TileArcFurnace, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    if (te.isController)
      renderAtLocation(x, y, z)
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
    if (model != null) model.render(false)
    GL11.glPopMatrix()
  }

  override def renderInProgressAt(x: Double, y: Double, z: Double, partialTime: Float, frame: TileFrame): Unit = {

    val inProgressModel = LoadObj(ArcFurnaceRenderer.inProgressModelLoc)

    Minecraft.getMinecraft.getTextureManager.bindTexture(ArcFurnaceRenderer.inProgressTexLoc)

        GL11.glPushMatrix()
        GL11.glTranslated(x + 1, y, z + 1)
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glColor4f(1f, 1f, 1f, 1f)

        val timePerPart = frame.totalMachineBuildTime / inProgressModel.getMatLib.getGroups.size()
        val currentPart = math.ceil(frame.progress / inProgressModel.getMatLib.getGroups.size().toDouble).toInt

        if (currentPart > 1) {
          for (i <- 1 until currentPart) {
            inProgressModel.renderGroups(Set("Stage" + (if (i < 10) "0" else "") + i))
          }
        }
        val time = frame.getWorld.getTotalWorldTime + partialTime
        if (currentPart != frame.inProgressData.getOrElseUpdate("lastPart", 0)) {
          frame.inProgressData("targetTime") = time + timePerPart
          frame.inProgressData("lastPart") = currentPart
        }
        GL11.glColor4ub(255.toByte, 255.toByte, 255.toByte, (256 - (256 / math.min(16f, timePerPart)) * math.min(math.min(16f, timePerPart), frame.inProgressData.getOrElseUpdate("targetTime", 0f).asInstanceOf[Float] - time)).toByte)
        inProgressModel.renderGroups(Set("Stage" + (if (currentPart < 10) "0" else "") + currentPart))
        GL11.glEnable(GL11.GL_LIGHTING)
        GL11.glPopMatrix()
  }

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
    renderAtLocation(rx, ry, rz)
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

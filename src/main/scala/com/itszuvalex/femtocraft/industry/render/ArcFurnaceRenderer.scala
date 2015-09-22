package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.core.IFrameMultiblockRenderer
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
  val inProgressModelLoc = Resources.Model("_in-progress/arc furnace/Arc Furnace In-Progress.obj")
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

  override val previewModel = AdvancedModelLoader.loadModel(ArcFurnaceRenderer.inProgressModelLoc).asInstanceOf[WavefrontObject]

  override val previewTexture = ArcFurnaceRenderer.inProgressTexLoc

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

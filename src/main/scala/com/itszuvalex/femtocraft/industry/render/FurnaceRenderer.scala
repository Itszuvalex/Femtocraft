package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.industry.IFrameMultiblockRenderer
import com.itszuvalex.femtocraft.industry.tile.{TileFurnace, TileArcFurnace, TileFrame}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 2/13/2016.
  */
object FurnaceRenderer {
  val modelLoc   = Resources.Model("furnace/Furnace.obj")
  val textureLoc = Resources.Model("furnace/furnace.png")
}

class FurnaceRenderer extends TileEntitySpecialRenderer with IFrameMultiblockRenderer {
  val model = AdvancedModelLoader.loadModel(FurnaceRenderer.modelLoc)

  override def renderTileEntityAt(tile : TileEntity, x : Double, y : Double, z : Double, partialTime : Float): Unit = {
    tile match {
      case furnace: TileFurnace if furnace.isController =>
        renderAtLocation(x, y, z)
      case _ =>
    }
  }

  /**
    * Coordinates are the location to render at.  This is usually the facing off-set location that, if the player right-clicked, a block would be placed at.
    *
    * @param stack ItemStack of IPreviewable Item
    * @param world World
    * @param x     X Location
    * @param y     Y Location
    * @param z     Z Location
    * @param rx    X Render location
    * @param ry    Y Render location
    * @param rz    Z Render location
    */
  override def previewRenderAtWorldLocation(stack: ItemStack, world: World, x: Int, y: Int, z: Int, rx: Double, ry: Double, rz: Double): Unit = {
    renderAtLocation(rx, ry, rz)
  }

  /**
    *
    * @return Bounding box for rendering.  (X, Y, Z) (Length, Height, Width)
    */
  override def boundingBox: (Int, Int, Int) = (2, 3, 2)

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
    Minecraft.getMinecraft.getTextureManager.bindTexture(FurnaceRenderer.textureLoc)
    model.renderAll()
    GL11.glPopMatrix()
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
    renderAtLocation(x, y, z)
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
    GL11.glPushMatrix()
    GL11.glTranslated(rx + 1, ry, rz + 1)
    GL11.glColor3f(1, 1, 1)
    GL11.glScalef(1f/3f, 1f/3f, 1f/3f)
    Minecraft.getMinecraft.getTextureManager.bindTexture(FurnaceRenderer.textureLoc)
    model.renderAll()
    GL11.glPopMatrix()
  }
}

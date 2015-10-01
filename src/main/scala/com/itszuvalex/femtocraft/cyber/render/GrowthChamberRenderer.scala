package com.itszuvalex.femtocraft.cyber.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.core.Cyber.ICyberMachineRenderer
import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.femtocraft.cyber.tile.TileGrowthChamber
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.obj.WavefrontObject
import org.lwjgl.opengl.GL11

/**
 * Created by Alex on 01.10.2015.
 */
object GrowthChamberRenderer {
  val inProgressModel = AdvancedModelLoader.loadModel(Resources.Model("growth chamber placeholder/Growth Chamber Placeholder.obj")).asInstanceOf[WavefrontObject]
  val inProgressTex = Resources.Model("growth chamber placeholder/placeholder.png")
}

class GrowthChamberRenderer extends TileEntitySpecialRenderer with ICyberMachineRenderer {
  override def renderTileEntityAt(tile : TileEntity, x : Double, y : Double, z : Double, partialTime : Float): Unit = {
    tile match { case t: TileGrowthChamber => if (!t.isController) return; case _ => return }
    Minecraft.getMinecraft.getTextureManager.bindTexture(GrowthChamberRenderer.inProgressTex)
    GL11.glPushMatrix()
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glTranslated(x + 1, y, z + 1)

    GrowthChamberRenderer.inProgressModel.renderAll()

    GL11.glEnable(GL11.GL_BLEND)
    GL11.glPopMatrix()
  }

  /**
   *
   * @return Bounding box for rendering.  (X, Y, Z) (Length, Height, Width)
   */
  override def boundingBox: (Int, Int, Int) = Tuple3(2, 2, 2)

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
   * Render function for machine in-progress rendering.
   *
   * @param x xPos to render at
   * @param y yPos to render at
   * @param z zPos to render at
   * @param partialTime Partial tick time
   * @param baseController Controller TileCyberBase of the machine.
   *                       Store any data that should persist between render calls in `baseController.inProgressData`.
   *                       If there is a float named `targetTime` in there, after reaching 100% progress it will wait for that point in time to pass before it places the machine.
   */
  override def renderInProgressAt(x: Double, y: Double, z: Double, partialTime: Float, baseController: TileCyberBase): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(GrowthChamberRenderer.inProgressTex)
    GL11.glPushMatrix()
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glTranslated(x + 1, y, z + 1)

    val progressD = baseController.currentMachineBuildProgress / 100d
    GL11.glScaled(progressD, progressD, progressD)
    GrowthChamberRenderer.inProgressModel.renderAll()

    GL11.glEnable(GL11.GL_BLEND)
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

  }
}

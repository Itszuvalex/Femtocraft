package com.itszuvalex.femtocraft.cyber.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.core.Cyber.ICyberMachineRenderer
import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.femtocraft.cyber.tile.TileGraspingVines
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import org.lwjgl.opengl.GL11

/**
  * Created by Alex on 01.10.2015.
  */
object GraspingVinesRenderer {
  //  val model = AdvancedModelLoader.loadModel(Resources.Model("growth chamber/Growth Chamber.obj")).asInstanceOf[WavefrontObject]
  val texture            = Resources.Model("grasping_vines/Grasping Vines Template.png")
  //val testTex = Resources.Model("growth chamber/test.png")
  val vineTexture        = Resources.Texture("vine_beam.png")
  val vineColoredTexture = Resources.Texture("vine_beam_colored.png")
}

class GraspingVinesRenderer extends TileEntitySpecialRenderer with ICyberMachineRenderer with GraspingVineBeamRenderer {
  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTime: Float): Unit = {
    tile match {
      case t: TileGraspingVines if t.isController =>
        //        Minecraft.getMinecraft.getTextureManager.bindTexture(GrowthChamberRenderer.texture)
        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glTranslated(x, y, z)
        GL11.glColor4f(1f, 1f, 1f, 1f)

        GL11.glPopMatrix()

        Minecraft.getMinecraft.getTextureManager.bindTexture(GraspingVinesRenderer.vineTexture)
        renderBeamsToEntities(x, y, z, partialTime, t, t.grabbedSet.toSet, .1f, new Color(255.toByte, 255.toByte, 255.toByte, 255.toByte))

        Minecraft.getMinecraft.getTextureManager.bindTexture(GraspingVinesRenderer.vineColoredTexture)
        renderBeamsToEntities(x, y, z, partialTime, t, t.grabbedSet.toSet, .1f, new Color(255.toByte, 255.toByte, 255.toByte, 255.toByte))
      case _ =>
    }
  }

  /**
    *
    * @return Bounding box for rendering.  (X, Y, Z) (Length, Height, Width)
    */
  override def boundingBox: (Int, Int, Int) = (1, 2, 1)

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

  }

  /**
    * Render using information contained in stack.
    *
    * @param stack
    * @param rx
    * @param ry
    * @param rz
    */
  override def renderAsItem(stack: ItemStack, rx: Double, ry: Double, rz: Double): Unit = {}
}


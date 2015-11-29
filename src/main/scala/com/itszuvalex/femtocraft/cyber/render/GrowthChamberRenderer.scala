package com.itszuvalex.femtocraft.cyber.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.cyber.tile.{TileCyberBase, TileGrowthChamber}
import com.itszuvalex.femtocraft.cyber.{ICyberMachineRenderer, IRecipeRenderer}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.obj.WavefrontObject
import org.lwjgl.opengl.GL11

/**
  * Created by Alex on 01.10.2015.
  */
object GrowthChamberRenderer {
  val model   = AdvancedModelLoader.loadModel(Resources.Model("growth chamber/Growth Chamber.obj")).asInstanceOf[WavefrontObject]
  val texture = Resources.Model("growth chamber/Growth Chamber Template.png")
  //val testTex = Resources.Model("growth chamber/test.png")
}

class GrowthChamberRenderer extends TileEntitySpecialRenderer with ICyberMachineRenderer {
  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTime: Float): Unit = {
    tile match {case t: TileGrowthChamber => if (!t.isController) return; case _ => return}
    Minecraft.getMinecraft.getTextureManager.bindTexture(GrowthChamberRenderer.texture)
    GL11.glPushMatrix()
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glTranslated(x + 1, y, z + 1)
    GL11.glColor4f(1f, 1f, 1f, 1f)

    GrowthChamberRenderer.model.renderOnly("Base", "Top")

    /*
    Minecraft.getMinecraft.getTextureManager.bindTexture(GrowthChamberRenderer.testTex)
    GL11.glDisable(GL11.GL_CULL_FACE)
    GrowthChamberRenderer.model.renderAllExcept("Base", "Top", "Glass", "Sprinkler1", "Sprinkler2", "Sprinkler3")
    GL11.glEnable(GL11.GL_CULL_FACE)

    Minecraft.getMinecraft.getTextureManager.bindTexture(GrowthChamberRenderer.texture)
    */

    val recipe = tile.asInstanceOf[TileGrowthChamber].currentRecipe
    if (recipe != null) {
      recipe.renderType match {
        case 0 =>
        case 1 =>
          recipe.renderObj match {
            case rl: ResourceLocation =>
              Minecraft.getMinecraft.getTextureManager.bindTexture(rl)
              GL11.glDisable(GL11.GL_CULL_FACE)
              GrowthChamberRenderer.model.renderAllExcept("Base", "Top", "Glass", "Sprinkler1", "Sprinkler2", "Sprinkler3")
              GL11.glEnable(GL11.GL_CULL_FACE)
            case ar: Array[ResourceLocation] =>
              val ind = math.max(math.ceil(ar.length * (tile.asInstanceOf[TileGrowthChamber].progress / 100d)).toInt - 1, 0)
              Minecraft.getMinecraft.getTextureManager.bindTexture(ar(ind))
              GL11.glDisable(GL11.GL_CULL_FACE)
              GrowthChamberRenderer.model.renderAllExcept("Base", "Top", "Glass", "Sprinkler1", "Sprinkler2", "Sprinkler3")
              GL11.glEnable(GL11.GL_CULL_FACE)
          }
          Minecraft.getMinecraft.getTextureManager.bindTexture(GrowthChamberRenderer.texture)
        case 2 =>
          GL11.glPopMatrix()
          recipe.renderObj.asInstanceOf[IRecipeRenderer].renderAtCenterLocation(x + 1, y + .2, z + 1, partialTime, tile.asInstanceOf[TileGrowthChamber].progress)
          Minecraft.getMinecraft.getTextureManager.bindTexture(GrowthChamberRenderer.texture)
          GL11.glPushMatrix()
          GL11.glEnable(GL11.GL_BLEND)
          GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
          GL11.glEnable(GL11.GL_CULL_FACE)
          GL11.glTranslated(x + 1, y, z + 1)
          GL11.glColor4f(1f, 1f, 1f, 1f)
      }
    }

    if (Minecraft.getMinecraft.gameSettings.particleSetting == 0) {
      val time = tile.getWorldObj.getTotalWorldTime + partialTime

      GL11.glTranslated(0, 1.9, .6)
      GL11.glRotated((1 + math.sin(time * .05)) * 20, 1, 0, 0)
      GL11.glTranslated(0, -1.9, -.6)
      GrowthChamberRenderer.model.renderPart("Sprinkler1")
      GL11.glTranslated(0, 1.9, .6)
      GL11.glRotated((1 + math.sin(time * .05)) * -20, 1, 0, 0)
      GL11.glTranslated(0, -1.9, -.6)
      GL11.glTranslated(-.5196, 1.9, -.3)
      GL11.glRotated((1 + math.sin(time * .05 + 1)) * 20, -.577350269189626, 0, 1)
      GL11.glTranslated(.5196, -1.9, .3)
      GrowthChamberRenderer.model.renderPart("Sprinkler2")
      GL11.glTranslated(-.5196, 1.9, -.3)
      GL11.glRotated((1 + math.sin(time * .05 + 1)) * -20, -.577350269189626, 0, 1)
      GL11.glTranslated(.5196, -1.9, .3)
      GL11.glTranslated(.5196, 1.9, -.3)
      GL11.glRotated((1 + math.sin(time * .05 + 2)) * -20, .577350269189626, 0, 1)
      GL11.glTranslated(-.5196, -1.9, .3)
      GrowthChamberRenderer.model.renderPart("Sprinkler3")
      GL11.glTranslated(.5196, 1.9, -.3)
      GL11.glRotated((1 + math.sin(time * .05 + 2)) * 20, .577350269189626, 0, 1)
      GL11.glTranslated(-.5196, -1.9, .3)

      GL11.glTranslated(-(x + 1), -y, -(z + 1))
      Minecraft.getMinecraft.effectRenderer.renderParticles(Minecraft.getMinecraft.renderViewEntity, partialTime)
      Minecraft.getMinecraft.getTextureManager.bindTexture(GrowthChamberRenderer.texture)
      GL11.glEnable(GL11.GL_BLEND)
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
      GL11.glEnable(GL11.GL_CULL_FACE)
      GL11.glTranslated(x + 1, y, z + 1)
      GL11.glColor4f(1f, 1f, 1f, 1f)
    }

    GrowthChamberRenderer.model.renderPart("Glass")

    GL11.glPopMatrix()
  }

  /**
    *
    * @return Bounding box for rendering.  (X, Y, Z) (Length, Height, Width)
    */
  override def boundingBox: (Int, Int, Int) = (2, 2, 2)

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
    Minecraft.getMinecraft.getTextureManager.bindTexture(GrowthChamberRenderer.texture)
    GL11.glPushMatrix()
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glTranslated(x + 1, y, z + 1)

    val progressD = baseController.currentMachineBuildProgress / 100d
    GL11.glScaled(progressD, progressD, progressD)

    GrowthChamberRenderer.model.renderOnly("Base", "Top", "Sprinkler1", "Sprinkler2", "Sprinkler3")
    GrowthChamberRenderer.model.renderPart("Glass")

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

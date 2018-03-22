package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.IFrameMultiblockRenderer
import com.itszuvalex.femtocraft.industry.tile.{TileFrame, TileGerminationChamber}
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer.ObjRender
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.render.RenderUtils
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT)
object MultiblockGerminationChamberRenderer {
  val chamberModelLoc = Resources.CustomModelBlock("growth chamber/growth chamber.obj")
  val chamberTexLoc   = Resources.CustomModelBlockTex("growth chamber/growth chamber template.png")
  //val baseModelLoc    = Resources.CustomModelBlock("cyber base/base 2x2.obj")
  //val baseTexLoc      = Resources.CustomModelBlockTex("cyber base/base 2x2.png")
  //val baseTexColorLoc = Resources.CustomModelBlockTex("cyber base/base 2x2 color.png")
}

@SideOnly(Side.CLIENT)
class MultiblockGerminationChamberRenderer extends TileEntitySpecialRenderer[TileGerminationChamber] with IFrameMultiblockRenderer {
  val chamberModel = OBJDynamicRenderer.LoadObj(MultiblockGerminationChamberRenderer.chamberModelLoc)
  //val baseModel    = OBJDynamicRenderer.LoadObj(MultiblockGerminationChamberRenderer.baseModelLoc)

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
    * Coordinates to render at.  This is for things like generic menu rendering, etc.
    *
    * @param rx
    * @param ry
    * @param rz
    */
  override def renderAtLocation(rx: Double, ry: Double, rz: Double): Unit = {
    renderAtLocationInternal(rx, ry, rz, 0, Color(0, 0, 0, 0), 0)
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
    RenderUtils.glMatrixBlock {
      GL11.glScalef(1f / 3f, 1f / 3f, 1f / 3f)
      renderAtLocation(rx, ry, rz)
    }

  }

  /**
    *
    * @return Bounding box for rendering.  (X, Y, Z) (Length, Height, Width)
    */
  override def boundingBox: (Int, Int, Int) = (2, 3, 2)

  override def render(te: TileGerminationChamber, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float): Unit = {
    te match {case t: TileGerminationChamber => if (t.hasCapability(Capabilities.MULTIBLOCK_CAPABILITY, null) && !t.getCapability(Capabilities.MULTIBLOCK_CAPABILITY, null).isController(t.getLoc)) return; case _ => return}
    renderAtLocationInternal(x, y, z, te.getWorld.getWorldTime, if (te.hasCapability(Capabilities.COLORABLE, null)) te.getCapability(Capabilities.COLORABLE, null) else Color(0, 0, 0, 0), partialTicks)
  }

  private def renderAtLocationInternal(x: Double, y: Double, z: Double, worldTime: Long, color: Color, partialTicks: Float) = {
    RenderUtils.glMatrixBlock {
      GL11.glEnable(GL11.GL_BLEND)
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
      GL11.glEnable(GL11.GL_CULL_FACE)
      GL11.glTranslated(x + 1, y, z + 1)
//      Minecraft.getMinecraft.getTextureManager.bindTexture(MultiblockGerminationChamberRenderer.baseTexLoc)
//      GL11.glColor4f(1f, 1f, 1f, 1f)
//      baseModel.render()
//      Minecraft.getMinecraft.getTextureManager.bindTexture(MultiblockGerminationChamberRenderer.baseTexColorLoc)
//      GL11.glColor4ub(color.red, color.green, color.blue, 255.toByte)
//      baseModel.render()

      Minecraft.getMinecraft.getTextureManager.bindTexture(MultiblockGerminationChamberRenderer.chamberTexLoc)
      GL11.glColor4f(1f, 1f, 1f, 1f)

      chamberModel.renderGroups(Set("Base", "Middle", "Top"))

//          val recipe = te.currentRecipe
//          if (recipe != null) {
//            recipe.renderType match {
//              case 0 =>
//              case 1 =>
//                recipe.renderObj match {
//                  case rl: ResourceLocation =>
//                    Minecraft.getMinecraft.getTextureManager.bindTexture(rl)
//                    GL11.glDisable(GL11.GL_CULL_FACE)
//                    GrowthChamberRenderer.model.renderAllExcept("Base", "Top", "Glass", "Sprinkler1", "Sprinkler2", "Sprinkler3")
//                    GL11.glEnable(GL11.GL_CULL_FACE)
//                  case ar: Array[ResourceLocation] =>
//                    val ind = math.max(math.ceil(ar.length * (te.asInstanceOf[TileGrowthChamber].progress / 100d)).toInt - 1, 0)
//                    Minecraft.getMinecraft.getTextureManager.bindTexture(ar(ind))
//                    GL11.glDisable(GL11.GL_CULL_FACE)
//                    GrowthChamberRenderer.model.renderAllExcept("Base", "Top", "Glass", "Sprinkler1", "Sprinkler2", "Sprinkler3")
//                    GL11.glEnable(GL11.GL_CULL_FACE)
//                }
//                Minecraft.getMinecraft.getTextureManager.bindTexture(GrowthChamberRenderer.texture)
//              case 2 =>
//                GL11.glPopMatrix()
//                recipe.renderObj.asInstanceOf[IRecipeRenderer].renderAtCenterLocation(x + 1, y + .2, z + 1, partialTime, te.asInstanceOf[TileGrowthChamber].progress)
//                Minecraft.getMinecraft.getTextureManager.bindTexture(GrowthChamberRenderer.texture)
//                GL11.glPushMatrix()
//                GL11.glEnable(GL11.GL_BLEND)
//                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
//                GL11.glEnable(GL11.GL_CULL_FACE)
//                GL11.glTranslated(x + 1, y, z + 1)
//                GL11.glColor4f(1f, 1f, 1f, 1f)
//            }
//          }

      if (Minecraft.getMinecraft.gameSettings.particleSetting == 0) {
        val time = worldTime + partialTicks

        GL11.glTranslated(0, 1.9, .6)
        GL11.glRotated((1 + math.sin(time * .05)) * 20, 1, 0, 0)
        GL11.glTranslated(0, -1.9, -.6)
        chamberModel.renderGroups(Set("Sprinkler1"))
        GL11.glTranslated(0, 1.9, .6)
        GL11.glRotated((1 + math.sin(time * .05)) * -20, 1, 0, 0)
        GL11.glTranslated(0, -1.9, -.6)
        GL11.glTranslated(-.5196, 1.9, -.3)
        GL11.glRotated((1 + math.sin(time * .05 + 1)) * 20, -.577350269189626, 0, 1)
        GL11.glTranslated(.5196, -1.9, .3)
        chamberModel.renderGroups(Set("Sprinkler2"))
        GL11.glTranslated(-.5196, 1.9, -.3)
        GL11.glRotated((1 + math.sin(time * .05 + 1)) * -20, -.577350269189626, 0, 1)
        GL11.glTranslated(.5196, -1.9, .3)
        GL11.glTranslated(.5196, 1.9, -.3)
        GL11.glRotated((1 + math.sin(time * .05 + 2)) * -20, .577350269189626, 0, 1)
        GL11.glTranslated(-.5196, -1.9, .3)
        chamberModel.renderGroups(Set("Sprinkler3"))
        GL11.glTranslated(.5196, 1.9, -.3)
        GL11.glRotated((1 + math.sin(time * .05 + 2)) * 20, .577350269189626, 0, 1)
        GL11.glTranslated(-.5196, -1.9, .3)

        GL11.glTranslated(-(x + 1), -y, -(z + 1))
        Minecraft.getMinecraft.effectRenderer.renderParticles(Minecraft.getMinecraft.getRenderViewEntity, partialTicks)
        Minecraft.getMinecraft.getTextureManager.bindTexture(MultiblockGerminationChamberRenderer.chamberTexLoc)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glTranslated(x + 1, y, z + 1)
        GL11.glColor4f(1f, 1f, 1f, 1f)
      }

      chamberModel.renderGroups(Set("Glass"))

    }
  }
}

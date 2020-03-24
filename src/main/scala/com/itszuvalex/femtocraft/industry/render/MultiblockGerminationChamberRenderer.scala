package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.FemtoRenderSwitches
import com.itszuvalex.femtocraft.industry.IFrameMultiblockRenderer
import com.itszuvalex.femtocraft.industry.multiblocks.MultiblockGerminationChamber
import com.itszuvalex.femtocraft.industry.tile.{TileFrame, TileGerminationChamber}
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer.ObjRender
import com.itszuvalex.femtocraft.render.{FemtoRenderUtils, OBJDynamicRenderer}
import com.itszuvalex.femtocraft.{FemtoBlocks, Resources}
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.{Converter, IItemStack, ITileEntity}
import com.itszuvalex.itszulib.render.{RenderUtils, TileEntityCombinedRenderer}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.client.model.obj.OBJModel
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT)
object MultiblockGerminationChamberRenderer {
  val chamberModelLoc   : ResourceLocation = Resources.CustomModelBlock("growth chamber/growth chamber.obj")
  val chamberTexLoc     : ResourceLocation = Resources.CustomModelBlockTex("growth chamber/growth chamber.png")
  val chamberColorTexLoc: ResourceLocation = Resources.CustomModelBlockTex("growth chamber/growth chamber_color.png")
}

@SideOnly(Side.CLIENT)
class MultiblockGerminationChamberRenderer extends TileEntityCombinedRenderer[TileGerminationChamber] with IFrameMultiblockRenderer {
  val chamberModel: OBJModel               = OBJDynamicRenderer.LoadObj(MultiblockGerminationChamberRenderer.chamberModelLoc)
  var lastTe      : TileGerminationChamber = _

  /**
    * Coordinates are the location to render at.  This is usually the facing off-set location that, if the player right-clicked, a block would be placed at.
    *
    * @param stack ItemStack of IPreviewable Item
    * @param loc
    * @param rx    X Render location
    * @param ry    Y Render location
    * @param rz    Z Render location
    */
  override def previewRenderAtWorldLocation(stack: IItemStack, loc: Loc4, rx: Double, ry: Double, rz: Double): Unit = {
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
    *
    * @return Bounding box for rendering.  (X, Y, Z) (Length, Height, Width)
    */
  override def boundingBox: (Int, Int, Int) = (2, 3, 2)

  override def renderTileEntityAsItem(x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    renderAsItem(Converter.IItemStackFromItemStack(new ItemStack(FemtoBlocks.blockGerminationChamber)), x, y, z)
  }

  /**
    * Render using information contained in stack.
    *
    * @param stack
    * @param rx
    * @param ry
    * @param rz
    */
  override def renderAsItem(stack: IItemStack, rx: Double, ry: Double, rz: Double): Unit = {
    RenderUtils.glMatrixBlock {
      GL11.glTranslated(rx, ry, rz)
      val max = Array(MultiblockGerminationChamber.xSize, MultiblockGerminationChamber.ySize, MultiblockGerminationChamber.zSize).max
      GL11.glScalef(1f / max.toFloat, 1f / max.toFloat, 1f / max.toFloat)
      renderAtLocation(.5, 0, .5)
    }

  }

  /**
    * Coordinates to render at.  This is for things like generic menu rendering, etc.
    *
    * @param rx
    * @param ry
    * @param rz
    */
  override def renderAtLocation(rx: Double, ry: Double, rz: Double): Unit = {
    renderAtLocationInternal(rx, ry, rz, 0, true, true, Color(0, 0, 0, 0), 0)
  }

  private def renderAtLocationInternal(x: Double, y: Double, z: Double, worldTime: Long, renderBase: Boolean, renderGlass: Boolean, color: Color, partialTicks: Float) = {
    RenderUtils.glMatrixBlock {
      GL11.glEnable(GL11.GL_BLEND)
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
      GL11.glEnable(GL11.GL_CULL_FACE)
      GL11.glTranslated(x + 1, y, z + 1)
      if (renderBase) {
        //      Minecraft.getMinecraft.getTextureManager.bindTexture(MultiblockGerminationChamberRenderer.baseTexLoc)
        //      GL11.glColor4f(1f, 1f, 1f, 1f)
        //      baseModel.render()
        //      Minecraft.getMinecraft.getTextureManager.bindTexture(MultiblockGerminationChamberRenderer.baseTexColorLoc)
        //      GL11.glColor4ub(color.red, color.green, color.blue, 255.toByte)
        //      baseModel.render()

        Minecraft.getMinecraft.getTextureManager.bindTexture(MultiblockGerminationChamberRenderer.chamberTexLoc)
        //      GL11.glColor4f(1f, 1f, 1f, 1f)

        chamberModel.renderGroups(Set("Base", "Middle", "Top"))

        GL11.glPushAttrib(GL11.GL_CURRENT_BIT)
        FemtoRenderUtils.disableLightMaps()
        Minecraft.getMinecraft.getTextureManager.bindTexture(MultiblockGerminationChamberRenderer.chamberColorTexLoc)
        GL11.glColor4ub(color.red, color.green, color.blue, 255.toByte)
        chamberModel.renderGroups(Set("Base", "Middle", "Top"))
        GL11.glPopAttrib()
        FemtoRenderUtils.enableLightMap(Converter.ITileEntityFromTileEntity(lastTe))

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
          Minecraft.getMinecraft.getTextureManager.bindTexture(MultiblockGerminationChamberRenderer.chamberTexLoc)
          renderSprinklers(x, y, z, worldTime, partialTicks)
          GL11.glPushAttrib(GL11.GL_CURRENT_BIT)
          FemtoRenderUtils.disableLightMaps()
          Minecraft.getMinecraft.getTextureManager.bindTexture(MultiblockGerminationChamberRenderer.chamberColorTexLoc)
          GL11.glColor4ub(color.red, color.green, color.blue, 255.toByte)
          renderSprinklers(x, y, z, worldTime, partialTicks)
          GL11.glPopAttrib()
          FemtoRenderUtils.enableLightMap(Converter.ITileEntityFromTileEntity(lastTe))
        }

        //        RenderUtils.glMatrixBlock {
        //          GL11.glTranslated(-.5, 1.2, .5)
        //          RenderUtils.bindBlockTextures()
        //          val state = Blocks.WHEAT.getDefaultState.withProperty(BlockCrops.AGE, Integer.valueOf(7))
        //          val blockmodel = Minecraft.getMinecraft.getBlockRendererDispatcher.getModelForState(state)
        //          Minecraft.getMinecraft.getBlockRendererDispatcher.getBlockModelRenderer.renderModelBrightness(blockmodel, state, 1f, false)
        //        }
      }

      if (renderGlass) {
        GL11.glPushAttrib(GL11.GL_CURRENT_BIT)
        Minecraft.getMinecraft.getTextureManager.bindTexture(MultiblockGerminationChamberRenderer.chamberColorTexLoc)
        FemtoRenderUtils.disableLightMaps()
        GL11.glColor4ub(color.red, color.green, color.blue, 30.toByte)
        chamberModel.renderGroups(Set("Glass"))
        FemtoRenderUtils.enableLightMap(Converter.ITileEntityFromTileEntity(lastTe))
        GL11.glColor3f(1, 1, 1)
        Minecraft.getMinecraft.getTextureManager.bindTexture(MultiblockGerminationChamberRenderer.chamberTexLoc)
        chamberModel.renderGroups(Set("Glass"))
        GL11.glPopAttrib()
      }
    }
  }

  private def renderSprinklers(x: Double, y: Double, z: Double, worldTime: Long, partialTicks: Float) = {
    val time = worldTime + partialTicks
    RenderUtils.glMatrixBlock {
      GL11.glTranslated(0, 2.9, .6)
      GL11.glRotated((1 + math.sin(time * .05)) * 20, 1, 0, 0)
      GL11.glTranslated(0, -2.9, -.6)
      chamberModel.renderGroups(Set("Sprinkler1"))
      GL11.glTranslated(0, 2.9, .6)
      GL11.glRotated((1 + math.sin(time * .05)) * -20, 1, 0, 0)
      GL11.glTranslated(0, -2.9, -.6)
      GL11.glTranslated(-.5196, 2.9, -.3)
      GL11.glRotated((1 + math.sin(time * .05 + 1)) * 20, -.577350269189626, 0, 1)
      GL11.glTranslated(.5196, -2.9, .3)
      chamberModel.renderGroups(Set("Sprinkler2"))
      GL11.glTranslated(-.5196, 2.9, -.3)
      GL11.glRotated((1 + math.sin(time * .05 + 1)) * -20, -.577350269189626, 0, 1)
      GL11.glTranslated(.5196, -2.9, .3)
      GL11.glTranslated(.5196, 2.9, -.3)
      GL11.glRotated((1 + math.sin(time * .05 + 2)) * -20, .577350269189626, 0, 1)
      GL11.glTranslated(-.5196, -2.9, .3)
      chamberModel.renderGroups(Set("Sprinkler3"))
      GL11.glTranslated(.5196, 2.9, -.3)
      GL11.glRotated((1 + math.sin(time * .05 + 2)) * 20, .577350269189626, 0, 1)
      GL11.glTranslated(-.5196, -2.9, .3)

      GL11.glTranslated(-(x + 1), -y, -(z + 1))
      Minecraft.getMinecraft.effectRenderer.renderParticles(Minecraft.getMinecraft.getRenderViewEntity, partialTicks)
      Minecraft.getMinecraft.getTextureManager.bindTexture(MultiblockGerminationChamberRenderer.chamberTexLoc)
      GL11.glEnable(GL11.GL_BLEND)
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
      GL11.glEnable(GL11.GL_CULL_FACE)
      GL11.glTranslated(x + 1, y, z + 1)
      GL11.glColor4f(1f, 1f, 1f, 1f)
    }
  }

  override def renderTileEntityInWorld(te: TileGerminationChamber, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float): Unit = {
    lastTe = te
    te match {
      case t: TileGerminationChamber => if (t.hasCapability(ItszuLibCapabilities.TILE_MULTIBLOCK, null) && t.getCapability(ItszuLibCapabilities.TILE_MULTIBLOCK, null).isController(t.getLoc))
        if (MinecraftForgeClient.getRenderPass == 0) {
          renderAtLocationInternal(x, y, z, te.getWorld.getWorldTime, renderBase = true, renderGlass = false, if (te.hasCapability(ItszuLibCapabilities.COLORABLE, null)) te.getCapability(ItszuLibCapabilities.COLORABLE, null) else Color(0, 0, 0, 0), partialTicks)
        }
        else if (MinecraftForgeClient.getRenderPass == 1) {
          renderAtLocationInternal(x, y, z, te.getWorld.getWorldTime, renderBase = false, renderGlass = true, if (te.hasCapability(ItszuLibCapabilities.COLORABLE, null)) te.getCapability(ItszuLibCapabilities.COLORABLE, null) else Color(0, 0, 0, 0), partialTicks)
        }
      case _ => return
    }

    if (FemtoRenderSwitches.renderItemConfiguration && te.hasCapability(Capabilities.ITEM_STORAGE_CONFIGURABLE, null)) {
      FemtoRenderUtils.renderItemConfigOverlay(te.asInstanceOf[ITileEntity], x, y, z, te.getCapability(Capabilities.ITEM_STORAGE_CONFIGURABLE, null))
    }

    if (FemtoRenderSwitches.renderFluidConfiguration && te.hasCapability(Capabilities.FLUID_STORAGE_CONFIGURABLE, null)) {
      FemtoRenderUtils.renderFluidConfigOverlay(te.asInstanceOf[ITileEntity], x, y, z, te.getCapability(Capabilities.FLUID_STORAGE_CONFIGURABLE, null))
    }
  }
}

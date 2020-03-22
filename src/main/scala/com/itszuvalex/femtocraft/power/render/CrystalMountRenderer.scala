package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.api.{Capabilities, ManagerModules}
import com.itszuvalex.femtocraft.power.render.CrystalMountRenderer._
import com.itszuvalex.femtocraft.power.tile.TileCrystalMount
import com.itszuvalex.femtocraft.render.FemtoRenderUtils
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.render.{RenderUtils, ShaderUtils, TileEntityCombinedRenderer}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraftforge.client.MinecraftForgeClient
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
object CrystalMountRenderer {
  val crystalModelLocation = Resources.CustomModelBlock("crystal mount/crystal_mount.obj")
  //  val crystalTexLocation   = new ResourceLocation(Femtocraft.ID + ":" + "models/crystal mount/crystal_mount.png")
  val crystalTexLocation   = Resources.CustomModelBlockTex("crystal mount/crystal_mount.png")

  val topName     = "Top"
  val bottomName  = "Bottom"
  val mountName   = "Mount"
  val gripName    = "GripBase"
  val crystalName = "Crystal"
}

class CrystalMountRenderer extends TileEntityCombinedRenderer[TileCrystalMount] {
  val crystalModel = LoadObj(crystalModelLocation)

  override def renderTileEntityAsItem(x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    super.renderTileEntityAsItem(x, y, z, partialTicks)
    renderCrystalMountAt(null, x, y, z, Minecraft.getMinecraft.getRenderPartialTicks, Option(Minecraft.getMinecraft.world).map(_.getTotalWorldTime.toFloat).getOrElse(0f), hasTop = false, hasBottom = true, hasCrystal = false, Color(0, 0, 0, 0))
  }

  def renderCrystalMountAt(tile: TileCrystalMount, renderX: Double, renderY: Double, renderZ: Double, partialTicks: Float, time: Float, hasTop: Boolean, hasBottom: Boolean, hasCrystal: Boolean, color: Color): Unit = {
    GL11.glPushMatrix()
    RenderUtils.translationBlock(renderX + .5, renderY, renderZ + .5) {
      renderCrystalMount(tile, time + partialTicks, hasTop, hasBottom, hasCrystal, color)
    }
    GL11.glPopMatrix()
  }

  def renderCrystalMount(tile: TileCrystalMount, rot: Float, hasTop: Boolean, hasBottom: Boolean, hasCrystal: Boolean, color: Color): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(crystalTexLocation)
    GL11.glColor3f(1f, 1f, 1f)

    if (hasBottom || !hasTop) crystalModel.renderGroups(Set(bottomName + mountName))
    if (hasTop) crystalModel.renderGroups(Set(topName + mountName))

    GL11.glRotated(rot, 0, 1, 0)

    if (hasBottom || !hasTop) crystalModel.renderGroups(Set(bottomName + gripName))
    if (hasTop) crystalModel.renderGroups(Set(topName + gripName))

    GL11.glColor4ub(color.red, color.green, color.blue, 220.toByte)

    if (hasCrystal) {
      FemtoRenderUtils.disableLightMaps()
      crystalModel.renderGroups(Set(crystalName))
      FemtoRenderUtils.enableLightMap(tile)
    }
    else {
      FemtoRenderUtils.disableLightMaps()
      ShaderUtils.bindShader(ShaderUtils.portal)
      GL11.glPushMatrix()
      RenderUtils.translationBlock(0, .5, 0) {
        GL11.glRotatef(rot, .25f, 2f, 3f)
        GL11.glScalef(1, .5f, 1)
        val scale = (Math.abs(Math.sin(rot / 10f)).toFloat * .15f) + .95f
        GL11.glScalef(scale, scale, scale)
      }
      Minecraft.getMinecraft.getTextureManager.bindTexture(new ResourceLocation("textures/entity/end_portal.png"))
      crystalModel.renderGroups(Set(crystalName))
      GL11.glPopMatrix()
      ShaderUtils.releaseShader()
      FemtoRenderUtils.enableLightMap(tile)
    }
    GL11.glColor3f(1f, 1f, 1f)
  }

  override def renderTileEntityInWorld(te: TileCrystalMount, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float): Unit = {
    super.renderTileEntityInWorld(te, x, y, z, partialTicks, destroyStage, alpha)
    if (MinecraftForgeClient.getRenderPass == 0) {
      val stateAbove = te.getWorld.getBlockState(te.getLoc.getOffset(EnumFacing.UP).getPos)
      val stateBelow = te.getWorld.getBlockState(te.getLoc.getOffset(EnumFacing.DOWN).getPos)
      val renderAbove = stateAbove.getBlock.isSideSolid(stateAbove, te.getWorld, te.getLoc.getOffset(EnumFacing.UP).getPos, EnumFacing.DOWN)
      val renderBelow = stateBelow.getBlock.isSideSolid(stateBelow, te.getWorld, te.getLoc.getOffset(EnumFacing.DOWN).getPos, EnumFacing.UP)
      renderCrystalMountAt(te, x, y, z, partialTicks, te.getWorld.getTotalWorldTime.toFloat, renderAbove, renderBelow, te.getCrystalStack != null && !te.getCrystalStack.isEmpty, te.getCapability(ItszuLibCapabilities.COLORABLE, EnumFacing.UP))
    }

    te.getModule(ManagerModules.TILE_POWER_NODE, null).renderLocations.flatMap(loc => loc.getITileEntity()).withFilter(_.hasModule(ManagerModules.TILE_POWER_NODE, null)).
      foreach { t =>
        val cap = t.getModule(ManagerModules.TILE_POWER_NODE, null)
        if (MinecraftForgeClient.getRenderPass == 1) PowerNodeBeamRenderer.renderPowerBeamToChild(te, x, y, z, partialTicks, cap.getLoc)
      }

    te.getModule(ManagerModules.TILE_POWER_NODE, null).leafNodes(false).
      foreach { t =>
        if (MinecraftForgeClient.getRenderPass == 1) DiffusionNodeBeamRenderer.renderBeamToChild(te, x, y, z, partialTicks, t.getStorageLoc)
      }
  }

  //  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks): Unit = {
  //    GL11.glPushMatrix()
  //    GL11.glTranslated(0, -.25, 0)
  //    renderCrystalMount(0, hasTop = false, hasCrystal = false, new Color(0))
  //    GL11.glPopMatrix()
  //  }
  //
  //  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = false
}

package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.power.render.CrystalMountRenderer._
import com.itszuvalex.femtocraft.power.tile.TileCrystalMount
import com.itszuvalex.femtocraft.render.FemtoRenderUtils
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import com.itszuvalex.itszulib.render.{RenderUtils, TileEntityCombinedRenderer}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumFacing
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
    renderCrystalMountAt(null, x, y, z, Minecraft.getMinecraft.getRenderPartialTicks, Option(Minecraft.getMinecraft.theWorld).map(_.getTotalWorldTime.toFloat).getOrElse(0f), false, false, Color(0, 0, 0, 0))
  }

  override def renderTileEntityInWorld(te: TileCrystalMount, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    super.renderTileEntityInWorld(te, x, y, z, partialTicks, destroyStage)
    if (MinecraftForgeClient.getRenderPass == 0)
      renderCrystalMountAt(te, x, y, z, partialTicks, te.getWorld.getTotalWorldTime.toFloat, te.getPedestalLocations.contains(te.getLoc.getOffset(EnumFacing.UP)), te.getCrystalStack != null && !te.getCrystalStack.func_190926_b(), te.getCapability(Capabilities.COLORABLE, EnumFacing.UP))

    if (te.getCrystalStack != null && !te.getCrystalStack.func_190926_b()) {
      te.getCapability(Capabilities.POWER_NODE, null).renderLocations.flatMap(loc => loc.getTileEntity()).withFilter(_.hasCapability(Capabilities.POWER_NODE, null)).
        foreach { t =>
          val cap = t.getCapability(Capabilities.POWER_NODE, EnumFacing.UP)
          if (MinecraftForgeClient.getRenderPass == 1) PowerNodeBeamRenderer.renderPowerBeamToChild(te, x, y, z, partialTicks, cap.getLoc)
        }

      te.getCapability(Capabilities.POWER_NODE, null).leafNodes.
        foreach { t =>
          if (MinecraftForgeClient.getRenderPass == 1) DiffusionNodeBeamRenderer.renderBeamToChild(te, x, y, z, partialTicks, t.getStorageLoc)
        }
    }
  }

  def renderCrystalMountAt(tile: TileCrystalMount, renderX: Double, renderY: Double, renderZ: Double, partialTicks: Float, time: Float, hasTop: Boolean, hasCrystal: Boolean, color: Color): Unit = {
    GL11.glPushMatrix()
    RenderUtils.translationBlock(renderX + .5, renderY, renderZ + .5) {
      renderCrystalMount(tile, time + partialTicks, hasTop, hasCrystal, color)
    }
    GL11.glPopMatrix()
  }

  def renderCrystalMount(tile: TileCrystalMount, rot: Float, hasTop: Boolean, hasCrystal: Boolean, color: Color): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(crystalTexLocation)
    GL11.glColor3f(1f, 1f, 1f)

    crystalModel.renderGroups(Set(bottomName + mountName))
    if (hasTop) crystalModel.renderGroups(Set(topName + mountName))

    GL11.glRotated(rot, 0, 1, 0)

    crystalModel.renderGroups(Set(bottomName + gripName))
    if (hasTop) crystalModel.renderGroups(Set(topName + gripName))

    GL11.glColor4ub(color.red, color.green, color.blue, 220.toByte)

    FemtoRenderUtils.disableLightMaps()
    if (hasCrystal)
      crystalModel.renderGroups(Set(crystalName))
    FemtoRenderUtils.enableLightMap(tile)
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

package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.power.ICrystalMount
import com.itszuvalex.femtocraft.power.tile.TilePowerPedestal
import com.itszuvalex.femtocraft.render.FemtoRenderUtils
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.render.{RenderUtils, TileEntityCombinedRenderer}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
object PowerPedestalRenderer {
  val pedestalModelLocation      = Resources.CustomModelBlock("power pedestal/power_pedestal_2x_f.obj")
  //  val crystalTexLocation   = new ResourceLocation(Femtocraft.ID + ":" + "models/crystal mount/crystal_mount.png")
  val pedestalTexLocation        = Resources.CustomModelBlockTex("power pedestal/power_pedestal_2x.png")
  val pedestalColoredTexLocation = Resources.CustomModelBlockTex("power pedestal/power_pedestal_2x_colored.png")
}

class PowerPedestalRenderer extends TileEntityCombinedRenderer[TilePowerPedestal] {
  val pedestalModel = LoadObj(PowerPedestalRenderer.pedestalModelLocation)

  override def renderTileEntityAsItem(x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    super.renderTileEntityAsItem(x, y, z, partialTicks)
    renderPedestalAt(null, x, y, z, Color(0, 255.toByte, 255.toByte, 255.toByte))
  }

  override def renderTileEntityInWorld(te: TilePowerPedestal, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float): Unit = {
    super.renderTileEntityInWorld(te, x, y, z, partialTicks, destroyStage, alpha)
    val color = Option(te.mountLoc).map(_.getITileEntity(false) match {
      case Some(a: ICrystalMount) => a.getCapability(ItszuLibCapabilities.COLORABLE, EnumFacing.UP)
      case _ => Color(0.toByte, 255.toByte, 255.toByte, 255.toByte)
    }).getOrElse(Color(0, 0, 0, 0))
    renderPedestalAt(te, x, y, z, color)
  }

  def renderPedestalAt(te: TilePowerPedestal, x: Double, y: Double, z: Double, color: Color): Unit = {
    GL11.glPushMatrix()
    RenderUtils.translationBlock(x + .5, y, z + .5) {
      Minecraft.getMinecraft.getTextureManager.bindTexture(PowerPedestalRenderer.pedestalTexLocation)

      GL11.glColor3f(1f, 1f, 1f)

      pedestalModel.render()

      GL11.glColor3ub(color.red, color.green, color.blue)
      FemtoRenderUtils.disableLightMaps()
      Minecraft.getMinecraft.getTextureManager.bindTexture(PowerPedestalRenderer.pedestalColoredTexLocation)
      pedestalModel.render()
      FemtoRenderUtils.enableLightMap(te)
    }

    GL11.glPopMatrix()
  }

  //  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks): Unit = {
  //    GL11.glPushMatrix()
  //    GL11.glTranslated(0, -.5, 0)
  //    renderPedestalAt(0)
  //    GL11.glPopMatrix()
  //  }
  //
  //  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = {
  //    false
  //  }
}

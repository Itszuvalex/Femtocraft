package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.power.IPowerPedestal
import com.itszuvalex.femtocraft.power.tile.TilePowerSink
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import com.itszuvalex.itszulib.render.{RenderUtils, TileEntityCombinedRenderer}
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 1/28/2016.
  */
object PowerSinkRenderer {
  val modelLocation = Resources.CustomModelBlock("power sink/power_sink_f.obj")
  val texLocation   = Resources.CustomModelBlockTex("power sink/power_sink.png")

  val PART_FRAME       = "Frame"
  val PART_SPHERE      = "Sphere"
  val PART_TORUS_INNER = "InnerTorus"
  val PART_TORUS_OUTER = "OuterTorus"
}

class PowerSinkRenderer extends TileEntityCombinedRenderer[TilePowerSink] {
  val pedestalModel = LoadObj(PowerSinkRenderer.modelLocation)

  override def renderTileEntityAsItem(x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    super.renderTileEntityAsItem(x, y, z, partialTicks, destroyStage)
    renderSink(x, y, z, false, partialTicks)
  }

  override def renderTileEntityInWorld(te: TilePowerSink, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    super.renderTileEntityInWorld(te, x, y, z, partialTicks, destroyStage)
    val f2: Float = te.getWorld.getTotalWorldTime.toFloat + partialTicks
    val flipped = te.getLoc.getOffset(EnumFacing.UP).getTileEntity(true).exists(_.isInstanceOf[IPowerPedestal])
    renderSink(x, y, z, flipped, f2)
  }

  def renderSink(x: Double, y: Double, z: Double, flipped: Boolean, partialTicks: Float): Unit = {
    GL11.glPushMatrix()
    RenderUtils.translationBlock(x + .5, y, z + .5) {
      Minecraft.getMinecraft.getTextureManager.bindTexture(PowerSinkRenderer.texLocation)

      GL11.glColor3f(1f, 1f, 1f)

      if (flipped) {
        GL11.glTranslated(0, .5, 0)
        GL11.glRotated(180, 1, 0, 0)
        GL11.glTranslated(0, -.5, 0)
      }

      pedestalModel.renderGroups(Set(PowerSinkRenderer.PART_FRAME))
      pedestalModel.renderGroups(Set(PowerSinkRenderer.PART_SPHERE))

      GL11.glPushMatrix()
      RenderUtils.translationBlock(0, .5, 0) {
        GL11.glRotatef(partialTicks * 2, 1f, 0f, 0f)
      }
      pedestalModel.renderGroups(Set(PowerSinkRenderer.PART_TORUS_OUTER))
      RenderUtils.translationBlock(0, .5, 0) {
        GL11.glRotatef(partialTicks * 3, 0f, 0f, 1f)
      }
      pedestalModel.renderGroups(Set(PowerSinkRenderer.PART_TORUS_INNER))
      GL11.glPopMatrix()
    }
    GL11.glPopMatrix()
  }

  //  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks): Unit = {
  //    GL11.glPushMatrix()
  //    GL11.glTranslated(0, -.5, 0)
  //    renderSink(flipped = false, 0)
  //    GL11.glPopMatrix()
  //  }

  //  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = {
  //    false
  //  }
}

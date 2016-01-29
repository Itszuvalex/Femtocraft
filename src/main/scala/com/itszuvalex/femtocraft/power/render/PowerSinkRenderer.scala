package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.power.tile.TilePowerSink
import com.itszuvalex.femtocraft.render.RenderIDs
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.obj.WavefrontObject
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 1/28/2016.
  */
object PowerSinkRenderer {
  val modelLocation = Resources.Model("power sink/power_sink.obj")
  val texLocation   = Resources.Model("power sink/power_sink.png")

  val PART_FRAME       = "Frame"
  val PART_SPHERE      = "Sphere"
  val PART_TORUS_INNER = "InnerTorus"
  val PART_TORUS_OUTER = "OuterTorus"
}

class PowerSinkRenderer extends TileEntitySpecialRenderer with ISimpleBlockRenderingHandler {
  val pedestalModel = AdvancedModelLoader.loadModel(PowerSinkRenderer.modelLocation).asInstanceOf[WavefrontObject]

  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    GL11.glPushMatrix()
    GL11.glTranslated(x + .5, y, z + .5)
    val f2: Float = tile.getWorldObj.getTotalWorldTime.toFloat + partialTicks
    tile match {
      case sink: TilePowerSink =>
        renderSink(flipped = false, f2)
      case _ =>
    }
    GL11.glPopMatrix()
  }

  def renderSink(flipped: Boolean, partialTicks: Float): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerSinkRenderer.texLocation)

    GL11.glColor3f(1f, 1f, 1f)

    pedestalModel.renderPart(PowerSinkRenderer.PART_FRAME)
    pedestalModel.renderPart(PowerSinkRenderer.PART_SPHERE)

    GL11.glPushMatrix()
    GL11.glTranslated(0, .5, 0)
    GL11.glRotatef(partialTicks, 1f, 0f, 0f)
    GL11.glTranslated(0, -.5, 0)
    pedestalModel.renderPart(PowerSinkRenderer.PART_TORUS_OUTER)
    GL11.glTranslated(0, .5, 0)
    GL11.glRotatef(partialTicks, 0f, 0f, 1f)
    GL11.glTranslated(0, -.5, 0)
    pedestalModel.renderPart(PowerSinkRenderer.PART_TORUS_INNER)
    GL11.glPopMatrix()
  }

  override def getRenderId: Int = RenderIDs.powerSinkRendererID

  override def shouldRender3DInInventory(modelId: Int) = true

  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks): Unit = {
    GL11.glPushMatrix()
    GL11.glTranslated(0, -.5, 0)
    renderSink(flipped = false, 0)
    GL11.glPopMatrix()
  }

  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = {
    false
  }
}

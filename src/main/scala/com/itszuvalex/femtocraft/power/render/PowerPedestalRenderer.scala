package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.power.{ICrystalMount, IPowerPedestal}
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.util.Color
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
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
object PowerPedestalRenderer {
  val pedestalModelLocation      = Resources.Model("power pedestal/power_pedestal_2x.obj")
  //  val crystalTexLocation   = new ResourceLocation(Femtocraft.ID + ":" + "models/crystal mount/crystal_mount.png")
  val pedestalTexLocation        = Resources.Model("power pedestal/power_pedestal_2x.png")
  val pedestalColoredTexLocation = Resources.Model("power pedestal/power_pedestal_2x_colored.png")
}

class PowerPedestalRenderer extends TileEntitySpecialRenderer with ISimpleBlockRenderingHandler {
  val pedestalModel = AdvancedModelLoader.loadModel(PowerPedestalRenderer.pedestalModelLocation).asInstanceOf[WavefrontObject]

  override def renderTileEntityAt(tile: TileEntity, renderX: Double, renderY: Double, renderZ: Double, partialTicks: Float): Unit = {
    GL11.glPushMatrix()
    GL11.glTranslated(renderX + .5, renderY, renderZ + .5)

    tile match {
      case tile: IPowerPedestal =>
        val color = Option(tile.mountLoc).map(_.getTileEntity(false) match {
                                                case Some(a: ICrystalMount) => new Color(a.getColor)
                                                case _ => new Color(0, 255.toByte, 255.toByte, 255.toByte)
                                              }).getOrElse(new Color(0, 0, 0, 0))
        renderPedestalAt(color.toInt)
      case _ =>
    }

    GL11.glPopMatrix()

    true
  }

  def renderPedestalAt(icolor: Int): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerPedestalRenderer.pedestalTexLocation)

    GL11.glColor3f(1f, 1f, 1f)

    pedestalModel.renderAll()

    val color = new Color(icolor)
    GL11.glColor3ub(color.red, color.green, color.blue)
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerPedestalRenderer.pedestalColoredTexLocation)
    pedestalModel.renderAll()
  }

  override def getRenderId: Int = RenderIDs.powerPedestalID

  override def shouldRender3DInInventory(modelId: Int): Boolean = true

  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks): Unit = {
    GL11.glPushMatrix()
    GL11.glTranslated(0, -.5, 0)
    renderPedestalAt(0)
    GL11.glPopMatrix()
  }

  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = {
    false
  }
}

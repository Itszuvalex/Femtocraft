package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.power.ICrystalMount
import com.itszuvalex.femtocraft.power.render.CrystalMountRenderer._
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
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
object CrystalMountRenderer {
  val crystalModelLocation = Resources.Model("crystal mount/crystal_mount.obj")
  //  val crystalTexLocation   = new ResourceLocation(Femtocraft.ID + ":" + "models/crystal mount/crystal_mount.png")
  val crystalTexLocation   = Resources.Model("crystal mount/crystal_mount.png")

  val topName     = "Top"
  val bottomName  = "Bottom"
  val mountName   = "Mount"
  val gripName    = "GripBase"
  val crystalName = "Crystal"
}

class CrystalMountRenderer extends TileEntitySpecialRenderer with PowerNodeBeamRenderer with ISimpleBlockRenderingHandler {
  val crystalModel = AdvancedModelLoader.loadModel(crystalModelLocation).asInstanceOf[WavefrontObject]

  override def renderTileEntityAt(tile: TileEntity, renderX: Double, renderY: Double, renderZ: Double, partialTicks: Float): Unit = {
    tile match {
      case i: ICrystalMount =>
        renderCrystalMountAt(i, renderX, renderY, renderZ, partialTicks, i.getPedestalLocations.contains(i.getNodeLoc.getOffset(ForgeDirection.UP)))
        if (i.getCrystalStack != null)
          renderPowerBeams(i, renderX, renderY, renderZ, partialTicks)
      case _ =>
    }
  }

  def renderCrystalMountAt(tile: TileEntity with ICrystalMount, renderX: Double, renderY: Double, renderZ: Double, partialTicks: Float, hasTop: Boolean): Unit = {
    GL11.glPushMatrix()
    GL11.glTranslated(renderX + .5, renderY, renderZ + .5)
    renderCrystalMount(tile.getWorldObj.getTotalWorldTime.toFloat + partialTicks, hasTop, tile.getCrystalStack != null, new Color(tile.getColor))
    GL11.glPopMatrix()
  }

  def renderCrystalMount(rot: Float, hasTop: Boolean, hasCrystal: Boolean, color: Color): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(crystalTexLocation)
    GL11.glColor3f(1f, 1f, 1f)

    crystalModel.renderPart(bottomName + mountName)
    if (hasTop) crystalModel.renderPart(topName + mountName)

    GL11.glRotated(rot, 0, 1, 0)

    crystalModel.renderPart(bottomName + gripName)
    if (hasTop) crystalModel.renderPart(topName + gripName)

    GL11.glColor4ub(color.red, color.green, color.blue, 220.toByte)

    if (hasCrystal)
      crystalModel.renderPart(crystalName)
  }

  override def getRenderId: Int = RenderIDs.crystalMountRenderID

  override def shouldRender3DInInventory(modelId: Int): Boolean = true

  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks): Unit = {
    GL11.glPushMatrix()
    GL11.glTranslated(0, -.25, 0)
    renderCrystalMount(0, hasTop = false, hasCrystal = false, new Color(0))
    GL11.glPopMatrix()
  }

  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = false
}

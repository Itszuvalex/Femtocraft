package com.itszuvalex.femtocraft.nanite.render

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.node.IPowerNode
import com.itszuvalex.femtocraft.power.render.DiffusionNodeBeamRenderer
import com.itszuvalex.femtocraft.render.RenderIDs
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.obj.WavefrontObject
import org.lwjgl.opengl.GL11

/**
 * Created by Christopher on 8/29/2015.
 */
object NaniteHiveSmallRenderer {
  val hiveModelLocation = new ResourceLocation(Femtocraft.ID + ":" + "models/nanite hive small/Nanite Hive Small.obj")
  val hiveTexLocation   = new ResourceLocation(Femtocraft.ID + ":" + "models/nanite hive small/nanite hive small.png")
}

class NaniteHiveSmallRenderer extends TileEntitySpecialRenderer with ISimpleBlockRenderingHandler with DiffusionNodeBeamRenderer {
  val model = AdvancedModelLoader.loadModel(NaniteHiveSmallRenderer.hiveModelLocation).asInstanceOf[WavefrontObject]

  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTick: Float): Unit =
    tile match {
      case node: IPowerNode =>
        GL11.glPushMatrix()
        GL11.glTranslated(x + .5, y, z + .5)
        preRender()
        model.renderAll()
        GL11.glPopMatrix()
        renderDiffuseBeams(node, x, y, z, partialTick)
      case _                =>
    }


  override def getRenderId: Int = RenderIDs.naniteHiveSmallID

  override def shouldRender3DInInventory(modelId: Int): Boolean = true

  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks): Unit = {
    GL11.glPushMatrix()
    GL11.glTranslated(.5, 0, .5)
    preRender()
    model.renderAll()
    GL11.glPopMatrix()
  }

  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = {
    false
  }

  def preRender() = {
    GL11.glScalef(.025f, .025f, .025f)
    Minecraft.getMinecraft.getTextureManager.bindTexture(NaniteHiveSmallRenderer.hiveTexLocation)
  }
}

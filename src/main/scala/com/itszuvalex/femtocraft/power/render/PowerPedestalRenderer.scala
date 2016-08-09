package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.power.ICrystalMount
import com.itszuvalex.femtocraft.power.tile.TilePowerPedestal
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import com.itszuvalex.itszulib.render.RenderUtils
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraftforge.client.model.ModelLoaderRegistry
import net.minecraftforge.client.model.obj.OBJModel
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

class PowerPedestalRenderer extends TileEntitySpecialRenderer[TilePowerPedestal] {
  val pedestalModel = ModelLoaderRegistry.getModelOrMissing(PowerPedestalRenderer.pedestalModelLocation).asInstanceOf[OBJModel]

  override def renderTileEntityAt(te: TilePowerPedestal, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    GL11.glPushMatrix()
    RenderUtils.translationBlock(x + .5, y, z + .5) {

      val color = Option(te.mountLoc).map(_.getTileEntity(false) match {
        case Some(a: ICrystalMount) => new Color(a.getColor)
        case _ => new Color(0, 255.toByte, 255.toByte, 255.toByte)
      }).getOrElse(new Color(0, 0, 0, 0))
      renderPedestalAt(color.toInt)
    }

    GL11.glPopMatrix()

  }

  def renderPedestalAt(icolor: Int): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerPedestalRenderer.pedestalTexLocation)

    GL11.glColor3f(1f, 1f, 1f)

    pedestalModel.render()

    val color = new Color(icolor)
    GL11.glColor3ub(color.red, color.green, color.blue)
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerPedestalRenderer.pedestalColoredTexLocation)
    pedestalModel.render()
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

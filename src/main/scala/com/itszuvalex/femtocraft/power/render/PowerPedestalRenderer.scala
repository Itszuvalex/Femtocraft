package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.power.{ICrystalMount, IPowerPedestal}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
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

class PowerPedestalRenderer extends TileEntitySpecialRenderer {
  val pedestalModel = AdvancedModelLoader.loadModel(PowerPedestalRenderer.pedestalModelLocation).asInstanceOf[WavefrontObject]

  override def renderTileEntityAt(tile: TileEntity, renderX: Double, renderY: Double, renderZ: Double, partialTicks: Float): Unit = {
    tile match {
      case i: IPowerPedestal =>
        renderPedestalAt(i, renderX, renderY, renderZ, partialTicks)
      case _ =>
    }
  }

  def renderPedestalAt(tile: TileEntity with IPowerPedestal, renderX: Double, renderY: Double, renderZ: Double, partialTicks: Float): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerPedestalRenderer.pedestalTexLocation)
    //    val color = new Color(tile.mountLoc.getTileEntity(true) match {
    //                            case Some(i:ICrystalMount) => i.getColor
    //                            case _ => 0
    //                          })
    GL11.glPushMatrix()
    GL11.glDisable(GL11.GL_CULL_FACE)
    GL11.glTranslated(renderX + .5, renderY, renderZ + .5)

    GL11.glColor3f(1f, 1f, 1f)

    pedestalModel.renderAll()

    val color = Option(tile.mountLoc).map(_.getTileEntity(false) match {
                                            case Some(a: ICrystalMount) => new Color(a.getColor)
                                            case _ => new Color(0, 255.toByte, 255.toByte, 255.toByte)
                                          }).getOrElse(new Color(0, 0, 0, 0))
    GL11.glColor3ub(color.red, color.green, color.blue)
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerPedestalRenderer.pedestalColoredTexLocation)
    pedestalModel.renderAll()

    GL11.glPopMatrix()
  }

}
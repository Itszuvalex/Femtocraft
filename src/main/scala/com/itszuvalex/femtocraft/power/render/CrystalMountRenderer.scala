package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.power.ICrystalMount
import com.itszuvalex.femtocraft.power.render.CrystalMountRenderer._
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
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
  val crystalTexLocation   = Resources.TexBlock("BlockItemRepository_side.png")

  val topName     = "Top"
  val bottomName  = "Bottom"
  val mountName   = "Mount"
  val gripName    = "GripBase"
  val crystalName = "Crystal"
}

class CrystalMountRenderer extends TileEntitySpecialRenderer with PowerNodeBeamRenderer {
  val crystalModel = AdvancedModelLoader.loadModel(crystalModelLocation).asInstanceOf[WavefrontObject]

  override def renderTileEntityAt(tile: TileEntity, renderX: Double, renderY: Double, renderZ: Double, partialTicks: Float): Unit = {
    tile match {
      case i: ICrystalMount =>
        Minecraft.getMinecraft.getTextureManager.bindTexture(crystalTexLocation)
        renderCrystalMountAt(i, renderX, renderY, renderZ, partialTicks, i.getPedestalLocations.contains(i.getNodeLoc.getOffset(ForgeDirection.UP)))
        if (i.getCrystalStack != null)
          renderPowerBeams(i, renderX, renderY, renderZ, partialTicks)
      case _ =>
    }
  }

  def renderCrystalMountAt(tile: TileEntity with ICrystalMount, renderX: Double, renderY: Double, renderZ: Double, partialTicks: Float, hasTop: Boolean): Unit = {
    val color = new Color(tile.getColor)
    GL11.glPushMatrix()
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glDisable(GL11.GL_CULL_FACE)
    GL11.glTranslated(renderX + .5, renderY, renderZ + .5)
    //    GL11.glScaled(.01, .01, .01)

    GL11.glColor3f(1f, 1f, 1f)

    crystalModel.renderPart(bottomName + mountName)
    if (hasTop) crystalModel.renderPart(topName + mountName)

    val f2: Float = tile.getWorldObj.getTotalWorldTime.toFloat + partialTicks
    GL11.glRotated(f2, 0, 1, 0)

    crystalModel.renderPart(bottomName + gripName)
    if (hasTop) crystalModel.renderPart(topName + gripName)

    GL11.glColor4ub(color.red, color.green, color.blue, 220.toByte)

    if (tile.getCrystalStack != null)
      crystalModel.renderPart(crystalName)

    GL11.glEnable(GL11.GL_LIGHTING)
    GL11.glPopMatrix()
  }

}

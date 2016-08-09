package com.itszuvalex.femtocraft.cyber.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.cyber.tile.TileCyberBase
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.obj.OBJModel
import org.lwjgl.opengl.GL11

/**
  * Created by Alex on 28.09.2015.
  */
object CyberBaseRenderer {
  val smallBaseModelLoc: ResourceLocation = Resources.CustomModelBlock("cyber base/Base 1x1.obj")
  val smallBaseTexLoc  : ResourceLocation = Resources.CustomModelBlockTex("cyber base/Base 1x1 Template.png")
  val medBaseModelLoc  : ResourceLocation = Resources.CustomModelBlock("cyber base/Base 2x2.obj")
  val medBaseTexLoc    : ResourceLocation = Resources.CustomModelBlockTex("cyber base/Base 2x2 Template.png")
  val largeBaseModelLoc: ResourceLocation = Resources.CustomModelBlock("cyber base/Base 3x3.obj")
  val largeBaseTexLoc  : ResourceLocation = Resources.CustomModelBlockTex("cyber base/Base 3x3 Template.png")

  val smallBaseModel = LoadObj(smallBaseModelLoc)
  val medBaseModel   = LoadObj(medBaseModelLoc)
  val largeBaseModel = LoadObj(largeBaseModelLoc)

  def renderBase(tile: TileCyberBase, x: Double, y: Double, z: Double, partialTime: Float): Unit = {
    GL11.glPushMatrix()
    var model: OBJModel = null
    tile.size match {
      case 1 =>
        model = smallBaseModel
        Minecraft.getMinecraft.getTextureManager.bindTexture(smallBaseTexLoc)
        GL11.glTranslated(x + .5, y, z + .5)
      case 2 =>
        model = medBaseModel
        Minecraft.getMinecraft.getTextureManager.bindTexture(medBaseTexLoc)
        GL11.glTranslated(x + 1, y, z + 1)
      case 3 =>
        model = largeBaseModel
        Minecraft.getMinecraft.getTextureManager.bindTexture(largeBaseTexLoc)
        GL11.glTranslated(x + 1.5, y, z + 1.5)
      case _ =>
    }
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glColor4f(1f, 1f, 1f, 1f)

    model.render(false)

    GL11.glEnable(GL11.GL_BLEND)
    GL11.glPopMatrix()
  }
}

class CyberBaseRenderer extends TileEntitySpecialRenderer[TileCyberBase] {

  override def renderTileEntityAt(te: TileCyberBase, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    if (!te.isController) return
    CyberBaseRenderer.renderBase(te, x, y, z, partialTicks)
    //        if (base.currentlyBuildingMachine > -1 && base.currentMachineBuildProgress > 0) {
    //          CyberMachineRegistry.getMachine(base.machines(base.currentlyBuildingMachine)) match {
    //            case Some(machine) =>
    //              CyberMachineRendererRegistry.getRenderer(machine.multiblockRenderID) match {
    //                case Some(render) =>
    //                  render.renderInProgressAt(x, y + TileCyberBase.baseHeightMap(base.size) + base.machineSlotMap(base.currentlyBuildingMachine), z, partialTime, base)
    //                case _ =>
    //              }
    //            case _ =>
    //          }
    //        }
  }
}

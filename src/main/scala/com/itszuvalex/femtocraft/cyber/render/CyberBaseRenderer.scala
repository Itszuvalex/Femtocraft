package com.itszuvalex.femtocraft.cyber.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.cyber.tile.TileCyberBase
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.obj.WavefrontObject
import org.lwjgl.opengl.GL11

/**
  * Created by Alex on 28.09.2015.
  */
object CyberBaseRenderer {
  val smallBaseModelLoc: ResourceLocation = Resources.Model("cyber base/Base 1x1.obj")
  val smallBaseTexLoc  : ResourceLocation = Resources.Model("cyber base/Base 1x1 Template.png")
  val medBaseModelLoc  : ResourceLocation = Resources.Model("cyber base/Base 2x2.obj")
  val medBaseTexLoc    : ResourceLocation = Resources.Model("cyber base/Base 2x2 Template.png")
  val largeBaseModelLoc: ResourceLocation = Resources.Model("cyber base/Base 3x3.obj")
  val largeBaseTexLoc  : ResourceLocation = Resources.Model("cyber base/Base 3x3 Template.png")

  val smallBaseModel = AdvancedModelLoader.loadModel(smallBaseModelLoc).asInstanceOf[WavefrontObject]
  val medBaseModel   = AdvancedModelLoader.loadModel(medBaseModelLoc).asInstanceOf[WavefrontObject]
  val largeBaseModel = AdvancedModelLoader.loadModel(largeBaseModelLoc).asInstanceOf[WavefrontObject]

  def renderBase(tile: TileCyberBase, x: Double, y: Double, z: Double, partialTime: Float): Unit = {
    GL11.glPushMatrix()
    var model: WavefrontObject = null
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

    model.renderAll()

    GL11.glEnable(GL11.GL_BLEND)
    GL11.glPopMatrix()
  }
}

class CyberBaseRenderer extends TileEntitySpecialRenderer {
  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTime: Float): Unit = {
    tile match {
      case base: TileCyberBase =>
        if (!base.isController) return
        CyberBaseRenderer.renderBase(base, x, y, z, partialTime)
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
      case _ =>
    }
  }
}

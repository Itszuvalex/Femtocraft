package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.itszulib.container.ContainerBase
import com.itszuvalex.itszulib.gui.GuiBase
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.gui.Gui
import net.minecraft.tileentity.TileEntity
import org.lwjgl.opengl.GL11

/**
  * Created by Chris on 1/15/2017.
  */
class FemtoGuiBase(tile: TileEntity, c: ContainerBase) extends GuiBase(c) {
  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    //    GL11.glDisable(GL11.GL_BLEND)
    GL11.glDisable(GL11.GL_LIGHTING)
    //    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiNanoFurnace.texture)
    //    val k = (width - xSize) / 2
    //    val l = (height - ySize) / 2
    //        drawTexturedModalRect(k, l, 0, 2, xSize, ySize)
    drawBackgroundLayers()

    //    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiNanoFurnace.colorTexture)
    //    GL11.glColor4f((color.red & 255) / 255f, (color.green & 255) / 255f, (color.blue & 255) / 255f, 1)
    //    drawTexturedModalRect(k + 67, l + 48, 0, 0, 106, 107)
    //
    //    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiNanoFurnace.texture)
    //    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    //    val prog = (tile.getProgress / tile.getProgressMax * 13).toInt
    //    drawTexturedModalRect(k + 67, l + 26 + 13 - prog, 183, 13 - prog, 13, prog)
    //
    //    GL11.glScaled(.5, .5, .5)
    //    nameLabel.render(2 * (anchorX + nameLabel.anchorX), 2 * (anchorY + nameLabel.anchorY), mouseX - anchorX - nameLabel.anchorX, mouseY - anchorY - nameLabel.anchorY, partialTicks)
    //    GL11.glScaled(2, 2, 2)
    //    inputSlot.render(anchorX + inputSlot.anchorX, anchorY + inputSlot.anchorY, mouseX - anchorX - inputSlot.anchorX, mouseY - anchorY - inputSlot.anchorY, partialTicks)
    //    outputSlot.render(anchorX + outputSlot.anchorX, anchorY + outputSlot.anchorY, mouseX - anchorX - outputSlot.anchorX, mouseY - anchorY - outputSlot.anchorY, partialTicks)
    //    powerMeter.render(anchorX + powerMeter.anchorX, anchorY + powerMeter.anchorY, mouseX - anchorX - powerMeter.anchorX, mouseY - anchorY - powerMeter.anchorY, partialTicks)

    GL11.glEnable(GL11.GL_BLEND)
    super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY)
  }

  private def drawBackgroundLayers(): Unit = {
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    val blackColor = Color(255.toByte, 0, 0, 0).toInt
    Gui.drawRect(k, l, k + xSize, l + ySize, blackColor)

    var offset = 1
    if (tile.hasCapability(Capabilities.COLORABLE, null)) {
      val color = tile.getCapability(Capabilities.COLORABLE, null).toInt
      Gui.drawRect(k + offset, l + offset, k + xSize - offset, l + ySize - offset, color)
      offset += 1
    }

    val greyColor = Color(255.toByte, 30.toByte, 30.toByte, 30.toByte).toInt
    Gui.drawRect(k + offset, l + offset, k + xSize - offset, l + ySize - offset, greyColor)
  }
}

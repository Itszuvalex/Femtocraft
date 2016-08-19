package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.industry.container.ContainerNanoFurnace
import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace
import com.itszuvalex.itszulib.gui.{GuiBase, GuiItemStack, GuiLabel}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import org.lwjgl.opengl.GL11

/**
  * Created by Alex on 18.08.2016.
  */
object GuiNanoFurnace {
  val texture      = Resources.TexGui("GuiNanoFurnace.png")
  val colorTexture = Resources.TexGui("GuiNanoFurnaceColor.png")
}

class GuiNanoFurnace(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileNanoFurnace) extends GuiBase(new ContainerNanoFurnace(player, inv, tile)) {

  fontRendererObj = Minecraft.getMinecraft.fontRendererObj
  xSize = 183
  ySize = 161

  //TODO: Make actual "machine color"
  var color = new Color(Option(tile.getParent).map(_.getColor).getOrElse(tile.getColor)) //Color(255.toByte, 64, 255.toByte, 255.toByte)

  val nameLabel  = new GuiLabel(20, 12, fontRendererObj.getStringWidth("Nano Furnace"), fontRendererObj.FONT_HEIGHT, "Nano Furnace")
  val inputSlot  = new GuiItemStack(44, 23)
  val outputSlot = new GuiItemStack(85, 23)
  val powerMeter = new GuiPowerMeter(6, 22, tile.getBattery, color.toInt)

  val elems = List(nameLabel, inputSlot, outputSlot, powerMeter)
  add(elems: _*)
  elems.foreach(e => e.setShouldRender(false))

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glDisable(GL11.GL_LIGHTING)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiNanoFurnace.texture)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 2, xSize, ySize)

    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiNanoFurnace.colorTexture)
    GL11.glColor4f((color.red & 255) / 255f, (color.green & 255) / 255f, (color.blue & 255) / 255f, 1)
    drawTexturedModalRect(k + 67, l + 48, 0, 0, 106, 107)

    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiNanoFurnace.texture)
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    val prog = (tile.getProgress / tile.getProgressMax * 13).toInt
    drawTexturedModalRect(k + 67, l + 26 + 13 - prog, 183, 13 - prog, 13, prog)

    GL11.glScaled(.5, .5, .5)
    nameLabel.render(2 * (anchorX + nameLabel.anchorX), 2 * (anchorY + nameLabel.anchorY), mouseX - anchorX - nameLabel.anchorX, mouseY - anchorY - nameLabel.anchorY, partialTicks)
    GL11.glScaled(2, 2, 2)
    inputSlot.render(anchorX + inputSlot.anchorX, anchorY + inputSlot.anchorY, mouseX - anchorX - inputSlot.anchorX, mouseY - anchorY - inputSlot.anchorY, partialTicks)
    outputSlot.render(anchorX + outputSlot.anchorX, anchorY + outputSlot.anchorY, mouseX - anchorX - outputSlot.anchorX, mouseY - anchorY - outputSlot.anchorY, partialTicks)
    powerMeter.render(anchorX + powerMeter.anchorX, anchorY + powerMeter.anchorY, mouseX - anchorX - powerMeter.anchorX, mouseY - anchorY - powerMeter.anchorY, partialTicks)
  }

}

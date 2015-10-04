package com.itszuvalex.femtocraft.core.Cyber.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.core.Cyber.container.ContainerCyberBase
import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.itszulib.gui.{GuiItemStack, GuiBase}
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.{InventoryPlayer, EntityPlayer}
import org.lwjgl.opengl.GL11

/**
 * Created by Alex on 03.10.2015.
 */
object GuiCyberBase {
  val texture = Resources.TexGui("GuiCyberBase.png")
}

class GuiCyberBase(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileCyberBase) extends GuiBase(new ContainerCyberBase(player, inv, tile)) {

  val bufferSlotSize = tile.size + 1

  def frender: FontRenderer = {
    Minecraft.getMinecraft.fontRenderer
  }

  val inputSlots = {
    for (i <- 0 until 9) yield new GuiItemStack(7 + 18 * (i % 3), 25 + 18 * (i / 3), null)
  }.toSeq

  val bufferSlots = {
    for (i <- 0 until math.pow(bufferSlotSize, 2).toInt) yield new GuiItemStack(79 + 18 * (i % bufferSlotSize), 7 + 18 * (i / bufferSlotSize), null)
  }.toSeq

  inputSlots.foreach(_.setShouldRender(false))
  bufferSlots.foreach(_.setShouldRender(false))

  {
    val elems = List() ++ inputSlots ++ bufferSlots
    add(elems: _*)
  }

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiCyberBase.texture)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 0, xSize, ySize)
    RenderHelper.enableGUIStandardItemLighting()

    inputSlots.foreach(gui => gui.render(anchorX + gui.anchorX, anchorY + gui.anchorY, mouseX - anchorX - gui.anchorX, mouseY - anchorY - gui.anchorY, partialTicks))
    bufferSlots.foreach(gui => gui.render(anchorX + gui.anchorX, anchorY + gui.anchorY, mouseX - anchorX - gui.anchorX, mouseY - anchorY - gui.anchorY, partialTicks))
  }

}

package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.industry.FrameMultiblockRegistry
import com.itszuvalex.femtocraft.industry.container.ContainerFrame
import com.itszuvalex.femtocraft.industry.tile.TileFrame
import com.itszuvalex.itszulib.gui._
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher on 9/21/2015.
  */
object GuiFrame {
  val texture = Resources.TexGui("GuiInventoryBase.png")
}

class GuiFrame(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileFrame) extends GuiBase(new ContainerFrame(player, inv, tile)) {

  val nameLabel = new GuiLabel((panelWidth - frender.getStringWidth(tile.multiBlock)) / 2, 7,
                               frender.getStringWidth(tile.multiBlock), frender.FONT_HEIGHT,
                               tile.multiBlock)
  val requiredLabel = new GuiLabel((panelWidth - frender.getStringWidth("Required")) / 2, 9 + frender.FONT_HEIGHT,
                                   frender.getStringWidth("Required"), frender.FONT_HEIGHT,
                                   "Required")
  val multiblock    = FrameMultiblockRegistry.getMultiblock(tile.multiBlock)
  val reqItems      = multiblock match {
    case Some(m) =>
      m.getRequiredResources.map(new GuiItemStack(0, 0, _, false))
    case None => Seq[GuiElement]()
  }
  val layout        = new GuiFlowLayout(7, 11 + frender.FONT_HEIGHT * 2, panelWidth - 14, 18, reqItems: _*)
  val itemSlots =
    (0 until 9).map(i => new GuiItemStack(7 + 18 * i, 61, null)).toSeq

  def frender: FontRenderer = {
    Minecraft.getMinecraft.fontRenderer
  }

  itemSlots.foreach(_.setShouldRender(false))

  {
    val elements = List(nameLabel, requiredLabel, layout) ++ itemSlots

    add(
         elements: _*
       )
  }

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiFrame.texture)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 0, xSize, ySize)
    RenderHelper.enableGUIStandardItemLighting()

    itemSlots.foreach(gui => gui.render(anchorX + gui.anchorX, anchorY + gui.anchorY, mouseX - anchorX - gui.anchorX, mouseY - anchorY - gui.anchorY, partialTicks))
  }
}

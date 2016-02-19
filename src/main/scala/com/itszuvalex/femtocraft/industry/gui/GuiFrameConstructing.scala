package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.industry.container.ContainerFrameConstructing
import com.itszuvalex.femtocraft.industry.tile.TileFrame
import com.itszuvalex.femtocraft.util.ItemUtils
import com.itszuvalex.itszulib.gui.{GuiBase, GuiItemStack, GuiLabel}
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import org.lwjgl.opengl.{GL12, GL11}

/**
  * Created by Christopher Harris (Itszuvalex) on 2/18/2016.
  */
object GuiFrameConstructing {
  val texture = Resources.TexGui("GuiFrameConstruction.png")
}

class GuiFrameConstructing(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileFrame) extends GuiBase(new ContainerFrameConstructing(player, inv, tile)) {
  val multiblockNameString = if (tile.multiBlock == null) "Undefined" else tile.multiBlock
  val nameLabel            = new GuiLabel((panelWidth - frender.getStringWidth(multiblockNameString)) / 2, 10, frender.getStringWidth(multiblockNameString), frender.FONT_HEIGHT, multiblockNameString)
  val multibockRender      = new GuiItemStack(0, 0, ItemUtils.makeMultiblockItem(tile.multiBlock), false)
  val constructingString   = "Constructing..."
  val constructingLabel    = new GuiLabel((panelWidth - frender.getStringWidth(constructingString)) / 2, panelHeight - 30, frender.getStringWidth(constructingString), frender.FONT_HEIGHT, constructingString)

  add(nameLabel, constructingLabel)

  def frender: FontRenderer = {
    Minecraft.getMinecraft.fontRenderer
  }

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, p_146976_2_ : Int, p_146976_3_ : Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiFrameConstructing.texture)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 0, xSize, ySize)

    val xProg = (tile.progress * 152) / tile.totalMachineBuildTime

    drawTexturedModalRect(k + 12, l + 149, 0, 166, xProg, 5)

    val scaling = 7

    GL11.glPushMatrix()
    GL11.glTranslated(k + (panelWidth - 18 * scaling) / 2d, l + (panelHeight - 18 * scaling) / 2d, 0)
    GL11.glScaled(scaling, scaling, 1)
    multibockRender.render(0, 0, -1, -1, partialTicks)
    GL11.glPopMatrix()
  }
}

package com.itszuvalex.femtocraft.cyber.gui

import com.itszuvalex.femtocraft.cyber.container.ContainerCyberBase
import com.itszuvalex.femtocraft.cyber.tile.TileCyberBase
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.MessageOpenGui
import com.itszuvalex.femtocraft.{FemtoFluids, GuiIDs, Resources}
import com.itszuvalex.itszulib.gui._
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import org.lwjgl.opengl.GL11

/**
  * Created by Alex on 03.10.2015.
  */
object GuiCyberBase {
  val texture = Resources.TexGui("GuiCyberBase.png")
}

class GuiCyberBase(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileCyberBase) extends GuiBase(new ContainerCyberBase(player, inv, tile)) {

  xSize = 221
  ySize = 177

  val bufferSlotSize = tile.size + 1
  val name           = tile.size + "x" + tile.size + " Cyber Base"
  val nameLabel      = new GuiLabel((panelWidth - frender.getStringWidth(name)) / 2, 7,
                                    frender.getStringWidth(name), frender.FONT_HEIGHT,
                                    name)
  val inputSlots     = {
                         for (i <- 0 until 9) yield new GuiItemStack(88 + 18 * (i % 3), 36 + 18 * (i / 3), null)
                       }.toSeq

  nameLabel.setShouldRender(false)
  val bufferSlots   = {
                        for (i <- 0 until math.pow(bufferSlotSize, 2).toInt) yield new GuiItemStack(7 + 18 * (i % bufferSlotSize), 18 + 18 * (i / bufferSlotSize), null)
                      }.toSeq
  val cybermassTank = new GuiFluidTank(151, 21, this, tile, 0, 3, FemtoFluids.cybermass, true)

  inputSlots.foreach(_.setShouldRender(false))
  bufferSlots.foreach(_.setShouldRender(false))
  val bufferTank1               = new GuiFluidTank(176, 21, this, tile, 1, 3, null, true)
  val buildButton               = new GuiButton(171, 94, 45, 20, "Build Machine") {
    override def onMouseClick(mouseX: Int, mouseY: Int, button: Int) = if (super.onMouseClick(mouseX, mouseY, button)) {
      FemtoPacketHandler.INSTANCE.sendToServer(new MessageOpenGui(tile.info.x, tile.info.y, tile.info.z, tile.getWorldObj.provider.dimensionId, GuiIDs.CyberBaseBuildGuiID))
      true
    } else false
  }
  var bufferTank2: GuiFluidTank = null
  if (tile.size == 3) bufferTank2 = new GuiFluidTank(196, 21, this, tile, 2, 3, null, true)

  cybermassTank.setShouldRender(false)
  bufferTank1.setShouldRender(false)
  if (tile.size == 3) bufferTank2.setShouldRender(false)

  def frender: FontRenderer = {
    Minecraft.getMinecraft.fontRenderer
  }

  buildButton.setShouldRender(false)

  {
    val elems = List(nameLabel, cybermassTank, bufferTank1, buildButton) ++ inputSlots ++ bufferSlots
    add(elems: _*)
    if (tile.size == 3) add(bufferTank2)
  }

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glDisable(GL11.GL_LIGHTING)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiCyberBase.texture)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 0, xSize, ySize)
    //TODO: Blame Itszu :P
    //RenderHelper.enableGUIStandardItemLighting()

    nameLabel.render(anchorX + nameLabel.anchorX, anchorY + nameLabel.anchorY, mouseX - anchorX - nameLabel.anchorX, mouseY - anchorY - nameLabel.anchorY, partialTicks)
    cybermassTank.render(anchorX + cybermassTank.anchorX, anchorY + cybermassTank.anchorY, mouseX - anchorX - cybermassTank.anchorX, mouseY - anchorY - cybermassTank.anchorY, partialTicks)
    bufferTank1.render(anchorX + bufferTank1.anchorX, anchorY + bufferTank1.anchorY, mouseX - anchorX - bufferTank1.anchorX, mouseY - anchorY - bufferTank1.anchorY, partialTicks)
    if (tile.size == 3) bufferTank2.render(anchorX + bufferTank2.anchorX, anchorY + bufferTank2.anchorY, mouseX - anchorX - bufferTank2.anchorX, mouseY - anchorY - bufferTank2.anchorY, partialTicks)
    buildButton.render(anchorX + buildButton.anchorX, anchorY + buildButton.anchorY, mouseX - anchorX - buildButton.anchorX, mouseY - anchorY - buildButton.anchorY, partialTicks)
    inputSlots.foreach(gui => gui.render(anchorX + gui.anchorX, anchorY + gui.anchorY, mouseX - anchorX - gui.anchorX, mouseY - anchorY - gui.anchorY, partialTicks))
    bufferSlots.foreach(gui => gui.render(anchorX + gui.anchorX, anchorY + gui.anchorY, mouseX - anchorX - gui.anchorX, mouseY - anchorY - gui.anchorY, partialTicks))

    GL11.glEnable(GL11.GL_BLEND)
  }

}

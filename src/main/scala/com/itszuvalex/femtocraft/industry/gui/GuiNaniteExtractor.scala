package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.container.ContainerNaniteExtractor
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteExtractor
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.MessageDrainNanite
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import com.itszuvalex.itszulib.gui._
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11


object GuiNaniteExtractor {
  val texture      = Resources.TexGui("guinaniteextractor.png")
  val colorTexture = Resources.TexGui("GuiNaniteExtractorColor.png")
}

class GuiNaniteExtractor(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileNaniteExtractor) extends GuiBase(new ContainerNaniteExtractor(player, inv, tile, false)) {
  fontRendererObj = Minecraft.getMinecraft.fontRendererObj
  xSize = 183
  ySize = 161

  addGuiAndSync(tile.storage, 0, 43, 23)
  addPlayerInventorySlots(inv, 4, 75)

  //TODO: Make actual "machine color"
  var color = tile.getCapability(Capabilities.COLORABLE, EnumFacing.UP)

  val nameLabel   = new GuiLabel(20, 12, fontRendererObj.getStringWidth("Nanite Extractor"), fontRendererObj.FONT_HEIGHT, () => "Nanite Extractor")
  val inputSlot   = new GuiItemStack(44, 23) {override def itemStack = IItemStack.Empty}
  val powerMeter  = new GuiPowerMeter(6, 22, tile.getCapability(Capabilities.POWER_STORAGE, null), color.toInt)
  val drainButton = new GuiButton(85, 23, 45, 15, "Drain") {
    override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
      val ret = super.onMouseClick(mouseX, mouseY, button)
      if (ret) {
        FemtoPacketHandler.INSTANCE.sendToServer(new MessageDrainNanite(tile.getLoc, null))
      }
      ret
    }
  }

  val elems = List(nameLabel, inputSlot, powerMeter, drainButton)
  add(elems: _*)
  elems.foreach(e => e.setShouldRender(false))


  override def GuiID: Int = GuiIDs.TileNaniteExtractorID

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
//    GL11.glDisable(GL11.GL_BLEND)
    GL11.glDisable(GL11.GL_LIGHTING)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiNaniteExtractor.texture)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 2, xSize, ySize)

    //    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiNaniteExtractor.colorTexture)
    //    GL11.glColor4f((color.red & 255) / 255f, (color.green & 255) / 255f, (color.blue & 255) / 255f, 1)
    //    drawTexturedModalRect(k + 67, l + 48, 0, 0, 106, 107)

    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiNaniteExtractor.texture)
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    val prog = (tile.getProgress / tile.getProgressMax * 13).toInt
    drawTexturedModalRect(k + 67, l + 26 + 13 - prog, 183, 13 - prog, 13, prog)

    GL11.glScaled(.5, .5, .5)
    nameLabel.render(2 * (anchorX + nameLabel.anchorX), 2 * (anchorY + nameLabel.anchorY), mouseX - anchorX - nameLabel.anchorX, mouseY - anchorY - nameLabel.anchorY, partialTicks)
    GL11.glScaled(2, 2, 2)
    //    inputSlot.render(anchorX + inputSlot.anchorX, anchorY + inputSlot.anchorY, mouseX - anchorX - inputSlot.anchorX, mouseY - anchorY - inputSlot.anchorY, partialTicks)
    powerMeter.render(anchorX + powerMeter.anchorX, anchorY + powerMeter.anchorY, mouseX - anchorX - powerMeter.anchorX, mouseY - anchorY - powerMeter.anchorY, partialTicks)
    drainButton.render(anchorX + drainButton.anchorX, anchorY + drainButton.anchorY, mouseX - anchorX - drainButton.anchorX, mouseY - anchorY - drainButton.anchorY, partialTicks)

    super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY)
  }

}

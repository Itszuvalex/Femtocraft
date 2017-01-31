package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabNetwork}
import com.itszuvalex.femtocraft.industry.container.ContainerNaniteExtractor
import com.itszuvalex.femtocraft.industry.tile.TileNaniteExtractor
import com.itszuvalex.femtocraft.nanite.gui.GuiNaniteTank
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.MessageDrainNanite
import com.itszuvalex.itszulib.gui._
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing


class GuiNaniteExtractor(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileNaniteExtractor) extends FemtoGuiBase(tile, new ContainerNaniteExtractor(player, inv, tile, false)) {
  GuiTabNetwork.addToGuiTabBar(tabBar, tile)

  fontRendererObj = Minecraft.getMinecraft.fontRendererObj
  addGuiAndSync(tile.storage, 0, 43, 23)
  addPlayerInventorySlots(inv)

  //TODO: Make actual "machine color"
  var color: Color = tile.getCapability(Capabilities.COLORABLE, EnumFacing.UP)

  val progressBar = new GuiProgress(44 + 18, 23 + 7, 85 - (44 + 18), 4, () => (tile.getProgress / tile.getProgressMax).toFloat)
  progressBar.colorProgress = color.toInt
  add(progressBar)

  val nameLabel   = new GuiLabel(20, 4, fontRendererObj.getStringWidth("Nanite Extractor"), fontRendererObj.FONT_HEIGHT, () => "Nanite Extractor")
  val powerMeter  = new GuiPowerMeter(6, 22, tile.getCapability(Capabilities.POWER_STORAGE, null), color.toInt)
  val drainButton = new GuiButton(103, 24, 45, 16, "Drain") {
    override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
      val ret = super.onMouseClick(mouseX, mouseY, button)
      if (ret) {
        FemtoPacketHandler.INSTANCE.sendToServer(new MessageDrainNanite(tile.getLoc, null))
      }
      ret
    }
  }

  val naniteTank = new GuiNaniteTank(85, 16, tile.naniteStorageTank)
  naniteTank.color = color
  add(naniteTank)

  val elems = List(nameLabel, powerMeter, drainButton)
  add(elems: _*)

  override def GuiID: Int = GuiIDs.TileNaniteExtractorID
}

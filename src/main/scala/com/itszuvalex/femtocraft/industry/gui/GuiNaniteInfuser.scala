package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabNetwork, GuiTabSideConfig}
import com.itszuvalex.femtocraft.industry.container.ContainerNaniteInfuser
import com.itszuvalex.femtocraft.industry.tile.TileNaniteInfuser.TileNaniteInfuser
import com.itszuvalex.femtocraft.nanite.gui.GuiNaniteTank
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.MessageFillNanite
import com.itszuvalex.itszulib.gui.{GuiButton, GuiLabel, GuiProgress}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing

class GuiNaniteInfuser(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileNaniteInfuser) extends FemtoGuiBase(tile, new ContainerNaniteInfuser(player, inv, tile, false)) {
  GuiTabNetwork.addToGuiTabBar(tabBar, tile)
  GuiTabSideConfig.addToGuiTabBar(tabBar, tile)

  fontRenderer = Minecraft.getMinecraft.fontRenderer
  addGuiAndSync(tile.storage, 0, 43, 23)
  addGuiAndSync(tile.storage, 1, 85, 23)
  addPlayerInventorySlots(inv)

  var color: Color = tile.getCapability(Capabilities.COLORABLE, EnumFacing.UP)

  val progressBar = new GuiProgress(44 + 18, 23 + 7, 85 - (44 + 18), 4, () => (tile.getProgress / tile.getProgressMax).toFloat)
  progressBar.colorProgress = color.toInt
  add(progressBar)

  val nameLabel   = new GuiLabel(20, 4, fontRenderer.getStringWidth("Nanite Infuser"), fontRenderer.FONT_HEIGHT, () => "Nanite Infuser")
  val powerMeter  = new GuiPowerMeter(6, 22, tile.getCapability(Capabilities.POWER_STORAGE, null), color.toInt)
  val drainButton = new GuiButton(103, 24, 45, 16, "Fill") {
    override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
      val ret = super.onMouseClick(mouseX, mouseY, button)
      if (ret) {
        FemtoPacketHandler.INSTANCE.sendToServer(new MessageFillNanite(tile.getLoc, null))
      }
      ret
    }
  }

  val naniteTank = new GuiNaniteTank(43, 42, tile.naniteStorageTank)
  naniteTank.color = color
  add(naniteTank)

  val elems = List(nameLabel, powerMeter, drainButton)
  add(elems: _*)

  override def GuiID: Int = GuiIDs.TileNaniteInfuserID
}

package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabInventorySideConfig, GuiTabNaniteSideConfig, GuiTabWirelessPowerNetwork}
import com.itszuvalex.femtocraft.industry.container.ContainerNaniteInfuser
import com.itszuvalex.femtocraft.industry.tile.{NaniteInfuserModule, TileNaniteInfuser}
import com.itszuvalex.femtocraft.nanite.gui.GuiNaniteTank
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.MessageFillNanite
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.gui.{GuiButton, GuiLabel, GuiProgress}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.TextFormatting

import scala.collection.mutable.ListBuffer

class GuiNaniteInfuser(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileNaniteInfuser) extends FemtoGuiBase(tile, new ContainerNaniteInfuser(player, inv, tile, false)) {
  GuiTabWirelessPowerNetwork.addToGuiTabBar(tabBar, tile)
  GuiTabInventorySideConfig.addToGuiTabBar(tabBar, tile)
  GuiTabNaniteSideConfig.addToGuiTabBar(tabBar, tile)

  var color: Color = tile.getModule(ItszuLibModules.COLORABLE, EnumFacing.UP)
  addGuiAndSync(tile.storage, 0, 43, 23)
  addGuiAndSync(tile.storage, 1, 85, 23)
  addPlayerInventorySlots(inv)

  val progressBar = new GuiProgress(44 + 18, 23 + 7, 85 - (44 + 18), 4, () => (tile.internal.getProgress / tile.internal.getProgressMax).toFloat) {
    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      super.addTooltip(mouseX, mouseY, tooltip)
      tooltip += f"${TextFormatting.WHITE}Progress${TextFormatting.RESET}: ${tile.internal.getProgress}%.2f/${tile.internal.getProgressMax}%.2f"
      tooltip += s"${TextFormatting.WHITE}Ticks${TextFormatting.RESET}: ${NaniteInfuserModule.TICKS_REQ}"
      tooltip += s"${TextFormatting.WHITE}PPT${TextFormatting.RESET}: ${NaniteInfuserModule.POWER_PER_TICK}"
    }
  }
  val nameLabel   = new GuiLabel(20, 4, fontRenderer.getStringWidth("Nanite Infuser"), fontRenderer.FONT_HEIGHT, () => "Nanite Infuser")
  progressBar.colorProgress = color.toInt
  add(progressBar)
  val powerMeter  = new GuiPowerMeter(6, 22, tile.getCapability(Capabilities.POWER_STORAGE, null), color.toInt)
  val drainButton = new GuiButton(43, 42, 45, 16, "Fill") {
    override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
      val ret = super.onMouseClick(mouseX, mouseY, button)
      if (ret) {
        FemtoPacketHandler.INSTANCE.sendToServer(new MessageFillNanite(tile.getLoc, null))
      }
      ret
    }
  }
  val naniteTank  = new GuiNaniteTank(26, 23, tile.naniteTank)
  val elems       = List(nameLabel, powerMeter, drainButton)
  naniteTank.color = color
  add(naniteTank)
  add(elems: _*)

  override def GuiID: Int = GuiIDs.TileNaniteInfuserID
}

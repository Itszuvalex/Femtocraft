package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabInventorySideConfig, GuiTabNetwork}
import com.itszuvalex.femtocraft.industry.container.ContainerDemolisher
import com.itszuvalex.femtocraft.industry.tile.TileDemolisher
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.gui.{GuiLabel, GuiProgress}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.TextFormatting

import scala.collection.mutable.ListBuffer

class GuiDemolisher(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileDemolisher) extends FemtoGuiBase(tile, new ContainerDemolisher(player, inv, tile, false)) {
  GuiTabNetwork.addToGuiTabBar(tabBar, tile)
  GuiTabInventorySideConfig.addToGuiTabBar(tabBar, tile)

  fontRenderer = Minecraft.getMinecraft.fontRenderer

  addGuiAndSync(tile.storage, 0, 44, 23)
  addGuiAndSync(tile.storage, 1, 85, 23)

  addPlayerInventorySlots(inv)

  val progressBar = new GuiProgress(44 + 18, 23 + 7, 85 - (44 + 18), 4, () => (tile.getProgress / tile.getProgressMax).toFloat) {
    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      super.addTooltip(mouseX, mouseY, tooltip)
      tooltip += f"${TextFormatting.WHITE}Progress${TextFormatting.RESET}: ${tile.getProgress}%.2f/${tile.getProgressMax}%.2f"
      tooltip += s"${TextFormatting.WHITE}Ticks${TextFormatting.RESET}: ${TileDemolisher.TICKS_REQ}"
      tooltip += s"${TextFormatting.WHITE}PPT${TextFormatting.RESET}: ${TileDemolisher.POWER_PER_TICK}"
    }
  }
  val nameLabel  = new GuiLabel(20, 12, fontRenderer.getStringWidth("Demolisher"), fontRenderer.FONT_HEIGHT, () => "Demolisher")
  val powerMeter = new GuiPowerMeter(6, 22, tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery, color.toInt)
  progressBar.colorProgress = color.toInt
  add(progressBar)
  val elems = List(nameLabel, powerMeter)
  //TODO: Make actual "machine color"
  var color: Color = tile.getCapability(ItszuLibCapabilities.COLORABLE, EnumFacing.UP)

  override def GuiID: Int = GuiIDs.TileDemolisherGuiID
  add(elems: _*)
  //  elems.foreach(e => e.setShouldRender(false))
}

package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabWirelessPowerNetwork}
import com.itszuvalex.femtocraft.industry.container.ContainerGerminationChamber
import com.itszuvalex.femtocraft.industry.tile.TileGerminationChamber
import com.itszuvalex.femtocraft.logistics.gui.GuiFluidTank
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.gui.{GuiLabel, GuiProgress}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.text.TextFormatting

import scala.collection.mutable.ListBuffer

/**
  * Created by Alex on 18.08.2016.
  */
class GuiGerminationChamber(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileGerminationChamber) extends FemtoGuiBase(tile, new ContainerGerminationChamber(player, inv, tile, false)) {
  GuiTabWirelessPowerNetwork.addToGuiTabBar(tabBar, tile)

  addGuiAndSync(tile.multiblockStorageModule.storage, 0, 48, 23)
  addGuiAndSync(tile.multiblockStorageModule.storage, 1, 90, 23)
  addGuiAndSync(tile.multiblockStorageModule.storage, 2, 90 + 18, 23)
  addGuiAndSync(tile.multiblockStorageModule.storage, 3, 90 + 18 * 2, 23)

  addPlayerInventorySlots(inv)

  val progressBar = new GuiProgress(44 + 22, 23 + 7, 85 - (44 + 18), 4, () => (tile.state.get.get.getProgress / tile.state.get.get.getProgressMax).toFloat) {
    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      super.addTooltip(mouseX, mouseY, tooltip)
      tooltip += f"${TextFormatting.WHITE}Progress${TextFormatting.RESET}: ${tile.state.get.get.getProgress}%.2f/${tile.state.get.get.getProgressMax}%.2f"
      tooltip += s"${TextFormatting.WHITE}Ticks${TextFormatting.RESET}: ${tile.state.get.get.getTicksMax}"
      tooltip += s"${TextFormatting.WHITE}PPT${TextFormatting.RESET}: ${TileGerminationChamber.POWER_PER_TICK}"
    }
  }
  val nameLabel   = new GuiLabel(3, 3, fontRenderer.getStringWidth("Germination Chamber"), fontRenderer.FONT_HEIGHT, () => "Germination Chamber")

  var color: Color = tile.getModule(ItszuLibModules.COLORABLE, null)
  progressBar.colorProgress = color.toInt
  add(progressBar)
  val powerMeter = new GuiPowerMeter(6, 22, tile.getModule(ManagerModules.POWER_STORAGE, null), color.toInt)
  add(nameLabel)
  val tank = new GuiFluidTank(28, fontRenderer.FONT_HEIGHT + 2, this, tile.multiblockFluidModule.storage, 0, true)
  add(powerMeter)
  add(tank)

  override def GuiID: Int = GuiIDs.TileGerminationChamberID
}


package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.FemtoGuiBase
import com.itszuvalex.femtocraft.industry.container.ContainerGerminationChamber
import com.itszuvalex.femtocraft.industry.tile.TileGerminationChamber
import com.itszuvalex.femtocraft.logistics.gui.GuiFluidTank
import com.itszuvalex.itszulib.gui.GuiLabel
import com.itszuvalex.itszulib.util
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}

import scala.collection.mutable.ListBuffer

/**
  * Created by Alex on 18.08.2016.
  */
class GuiGerminationChamber(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileGerminationChamber) extends FemtoGuiBase(tile, new ContainerGerminationChamber(player, inv, tile, false)) {
  //  GuiTabNetwork.addToGuiTabBar(tabBar, tile)

  fontRenderer = Minecraft.getMinecraft.fontRenderer

  addGuiAndSync(tile.storage, 0, 48, 23)
  addGuiAndSync(tile.storage, 1, 90, 23)

  addPlayerInventorySlots(inv)

  //  var color: Color = tile.getCapability(com.itszuvalex.itszulib.api.Capabilities.COLORABLE, EnumFacing.UP)

  //  val progressBar = new GuiProgress(44 + 18, 23 + 7, 85 - (44 + 18), 4, () => (tile.getProgress / tile.getProgressMax).toFloat) {
  override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
    super.addTooltip(mouseX, mouseY, tooltip)
    //      tooltip += f"${TextFormatting.WHITE}Progress${TextFormatting.RESET}: ${tile.getProgress}%.2f/${tile.getProgressMax}%.2f"
    //      tooltip += s"${TextFormatting.WHITE}Ticks${TextFormatting.RESET}: ${TileGerminationChamber.TICKS_REQ}"
    //      tooltip += s"${TextFormatting.WHITE}PPT${TextFormatting.RESET}: ${TileGerminationChamber.POWER_PER_TICK}"
    //    }
  }

  //  progressBar.colorProgress = color.toInt
  //  add(progressBar)

  val nameLabel = new GuiLabel(3, 3, fontRenderer.getStringWidth("Germination Chamber"), fontRenderer.FONT_HEIGHT, () => "Germination Chamber")
  add(nameLabel)
  val powerMeter = new GuiPowerMeter(6, 22, tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery, util.Color(0, 0, 0, 0).toInt)
  add(powerMeter)

  val tank = new GuiFluidTank(28, fontRenderer.FONT_HEIGHT + 2, this, tile.getCapability(com.itszuvalex.itszulib.api.Capabilities.FLUID_STORAGE, null), 0, true)
  add(tank)

  override def GuiID: Int = GuiIDs.TileGerminationChamberID
}

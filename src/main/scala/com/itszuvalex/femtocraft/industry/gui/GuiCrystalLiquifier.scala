package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabFluidSideConfig, GuiTabInventorySideConfig}
import com.itszuvalex.femtocraft.industry.container.ContainerCrystalLiquifier
import com.itszuvalex.femtocraft.industry.tile.{CrystalLiquifierModule, TileCrystalLiquifier}
import com.itszuvalex.femtocraft.logistics.gui.GuiFluidTank
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.gui.{GuiLabel, GuiProgress}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.TextFormatting

import scala.collection.mutable.ListBuffer

class GuiCrystalLiquifier(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileCrystalLiquifier) extends FemtoGuiBase(tile, new ContainerCrystalLiquifier(player, inv, tile, false)) {
  GuiTabInventorySideConfig.addToGuiTabBar(tabBar, tile)
  GuiTabFluidSideConfig.addToGuiTabBar(tabBar, tile)

  addGuiAndSync(tile.storage, 0, 44, 23)
  addGuiAndSync(tile.storage, 1, 6, 56)

  addPlayerInventorySlots(inv)

  val progressBar  = new GuiProgress(44 + 18, 23 + 7, 85 - (44 + 18), 4, () => (tile.internal.getProgress / tile.internal.getProgressMax).toFloat) {
    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      super.addTooltip(mouseX, mouseY, tooltip)
      tooltip += f"${TextFormatting.WHITE}Progress${TextFormatting.RESET}: ${tile.internal.getProgress}%.2f/${tile.internal.getProgressMax}%.2f"
      tooltip += s"${TextFormatting.WHITE}Ticks${TextFormatting.RESET}: ${CrystalLiquifierModule.TICKS_REQ}"
      tooltip += s"${TextFormatting.WHITE}PPT${TextFormatting.RESET}: ${CrystalLiquifierModule.POWER_PER_TICK}"
    }
  }
  var color: Color = tile.getModule(ItszuLibModules.COLORABLE, EnumFacing.UP)
  val nameLabel    = new GuiLabel(20, 12, fontRenderer.getStringWidth("Crystal Liquifier"), fontRenderer.FONT_HEIGHT, () => "Crystal Liquifier")
  val powerMeter   = new GuiPowerMeter(6, 4, tile.battery, color.toInt)
  val tank         = new GuiFluidTank(28, fontRenderer.FONT_HEIGHT + 2, this, tile.outputStorage, 0, true)
  progressBar.colorProgress = color.toInt
  add(progressBar)
  val elems = List(nameLabel, powerMeter, tank)
  //TODO: Make actual "machine color"

  override def GuiID: Int = GuiIDs.TileCrystalLiquifierID

  add(elems: _*)
  //  elems.foreach(e => e.setShouldRender(false))
}

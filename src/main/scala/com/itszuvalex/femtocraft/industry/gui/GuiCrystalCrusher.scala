package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabInventorySideConfig}
import com.itszuvalex.femtocraft.industry.container.ContainerCrystalCrusher
import com.itszuvalex.femtocraft.industry.tile.{CrystalCrusherModule, TileCrystalCrusher}
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.gui.{GuiLabel, GuiProgress}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.TextFormatting

import scala.collection.mutable.ListBuffer

class GuiCrystalCrusher(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileCrystalCrusher) extends FemtoGuiBase(tile, new ContainerCrystalCrusher(player, inv, tile, false)) {
  GuiTabInventorySideConfig.addToGuiTabBar(tabBar, tile)

  addGuiAndSync(tile.storage, 0, 44, 23)
  addGuiAndSync(tile.storage, 1, 85, 23)
  addGuiAndSync(tile.storage, 2, 6, 56)

  addPlayerInventorySlots(inv)

  val progressBar  = new GuiProgress(44 + 18, 23 + 7, 85 - (44 + 18), 4, () => (tile.internal.getProgress / tile.internal.getProgressMax).toFloat) {
    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      super.addTooltip(mouseX, mouseY, tooltip)
      tooltip += f"${TextFormatting.WHITE}Progress${TextFormatting.RESET}: ${tile.internal.getProgress}%.2f/${tile.internal.getProgressMax}%.2f"
      tooltip += s"${TextFormatting.WHITE}Ticks${TextFormatting.RESET}: ${CrystalCrusherModule.TICKS_REQ}"
      tooltip += s"${TextFormatting.WHITE}PPT${TextFormatting.RESET}: ${CrystalCrusherModule.POWER_PER_TICK}"
    }
  }
  var color: Color = tile.getModule(ItszuLibModules.COLORABLE, EnumFacing.UP)
  val nameLabel    = new GuiLabel(20, 12, fontRenderer.getStringWidth("Crystal Crusher"), fontRenderer.FONT_HEIGHT, () => "Crystal Crusher")
  val powerMeter   = new GuiPowerMeter(6, 4, tile.battery, color.toInt)
  progressBar.colorProgress = color.toInt
  add(progressBar)
  val elems = List(nameLabel, powerMeter)
  //TODO: Make actual "machine color"

  override def GuiID: Int = GuiIDs.TileCrystalCrusherID

  add(elems: _*)
  //  elems.foreach(e => e.setShouldRender(false))
}

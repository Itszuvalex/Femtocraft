package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabInventorySideConfig}
import com.itszuvalex.femtocraft.industry.container.ContainerCrystalFurnace
import com.itszuvalex.femtocraft.industry.tile.{CrystalFurnaceModule, TileCrystalFurnace}
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.gui.{GuiLabel, GuiProgress}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.TextFormatting

import scala.collection.mutable.ListBuffer

/**
 * Created by Alex on 18.08.2016.
 */
class GuiCrystalFurnace(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileCrystalFurnace) extends FemtoGuiBase(tile, new ContainerCrystalFurnace(player, inv, tile, false)) {
  GuiTabInventorySideConfig.addToGuiTabBar(tabBar, tile)

  fontRenderer = Minecraft.getMinecraft.fontRenderer

  addGuiAndSync(tile.storage, 0, 44, 23)
  addGuiAndSync(tile.storage, 1, 85, 23)
  addGuiAndSync(tile.storage, 2, 6, 56)

  addPlayerInventorySlots(inv)

  val progressBar  = new GuiProgress(44 + 18, 23 + 7, 85 - (44 + 18), 4, () => (tile.internal.getProgress / tile.internal.getProgressMax).toFloat) {
    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      super.addTooltip(mouseX, mouseY, tooltip)
      tooltip += f"${TextFormatting.WHITE}Progress${TextFormatting.RESET}: ${tile.internal.getProgress}%.2f/${tile.internal.getProgressMax}%.2f"
      tooltip += s"${TextFormatting.WHITE}Ticks${TextFormatting.RESET}: ${CrystalFurnaceModule.TICKS_REQ}"
      tooltip += s"${TextFormatting.WHITE}PPT${TextFormatting.RESET}: ${CrystalFurnaceModule.POWER_PER_TICK}"
    }
  }
  var color: Color = tile.getModule(ItszuLibModules.COLORABLE, EnumFacing.UP)
  val nameLabel    = new GuiLabel(20, 12, fontRenderer.getStringWidth("Crystal Furnace"), fontRenderer.FONT_HEIGHT, () => "Crystal Furnace")
  progressBar.colorProgress = color.toInt
  add(progressBar)
  val powerMeter = new GuiPowerMeter(6, 4, tile.battery, color.toInt)
  val elems      = List(nameLabel, powerMeter)
  add(elems: _*)

  //  elems.foreach(e => e.setShouldRender(false))
  override def GuiID: Int = GuiIDs.TileCrystalFurnaceID
}

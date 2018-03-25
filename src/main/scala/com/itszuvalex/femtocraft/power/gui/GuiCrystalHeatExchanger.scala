package com.itszuvalex.femtocraft.power.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabNetwork}
import com.itszuvalex.femtocraft.industry.gui.GuiPowerMeter
import com.itszuvalex.femtocraft.power.container.ContainerCrystalHeatExchanger
import com.itszuvalex.femtocraft.power.tile.TileCrystalHeatExchanger
import com.itszuvalex.itszulib.gui.{GuiLabel, GuiProgress}
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}

class GuiCrystalHeatExchanger(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileCrystalHeatExchanger)
  extends FemtoGuiBase(tile, new ContainerCrystalHeatExchanger(player, inv, tile, false)) {
  GuiTabNetwork.addToGuiTabBar(tabBar, tile)

  tile.storage.indices.foreach(i =>
    addGuiAndSync(tile.storage, i, 61 + 18 * (i % 3), 23 + 18 * (i / 3))
  )
  addPlayerInventorySlots(inv)

  val progressGui = new GuiProgress(58, 23, 3, 18, () => tile.getBurnTime.toFloat / tile.getBurnMax.toFloat, direction = GuiProgress.BottomUp)
  progressGui.colorProgress = tile.getCapability(com.itszuvalex.itszulib.api.Capabilities.COLORABLE, null).toInt
  add(progressGui)

  val powerMeter   = new GuiPowerMeter(6, 22, tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery, tile.getCapability(com.itszuvalex.itszulib.api.Capabilities.COLORABLE, null).toInt)
  val powerReading = new GuiLabel(6, 14, 80, Minecraft.getMinecraft.fontRenderer.FONT_HEIGHT, labelText)
  add(powerReading, powerMeter)

  override def GuiID: Int = GuiIDs.TileCrystalHeatExchangerID

  def labelText(): String = "%.1f".format(tile.powerPerTick) + " DE/t"
}

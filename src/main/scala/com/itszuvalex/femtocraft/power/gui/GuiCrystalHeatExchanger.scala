package com.itszuvalex.femtocraft.power.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabNetwork}
import com.itszuvalex.femtocraft.industry.gui.GuiPowerMeter
import com.itszuvalex.femtocraft.power.container.ContainerCrystalHeatExchanger
import com.itszuvalex.femtocraft.power.tile.TileCrystalHeatExchanger
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.gui.{GuiLabel, GuiProgress}
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}

class GuiCrystalHeatExchanger(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileCrystalHeatExchanger)
  extends FemtoGuiBase(tile, new ContainerCrystalHeatExchanger(player, inv, tile, false)) {
  GuiTabNetwork.addToGuiTabBar(tabBar, tile)

  val storage: IItemStorage = tile.getModule(ItszuLibModules.ITEM_STORAGE, null)
  storage.indices.foreach(i =>
                            addGuiAndSync(storage, i, 61 + 18 * (i % 3), 23 + 18 * (i / 3))
                          )
  addPlayerInventorySlots(inv)

  val progressGui = new GuiProgress(58, 23, 3, 18, () => tile.internal.getBurnTime.toFloat / tile.internal.getBurnMax.toFloat, direction = GuiProgress.BottomUp)
  progressGui.colorProgress = tile.getModule(ItszuLibModules.COLORABLE, null).toInt
  add(progressGui)

  val powerMeter   = new GuiPowerMeter(6, 22, tile.getModule(ManagerModules.TILE_POWER_STORAGE_NODE, null).battery, tile.getModule(ItszuLibModules.COLORABLE, null).toInt)
  val powerReading = new GuiLabel(6, 14, 80, Minecraft.getMinecraft.fontRenderer.FONT_HEIGHT, labelText)
  add(powerReading, powerMeter)

  override def GuiID: Int = GuiIDs.TileCrystalHeatExchangerID

  def labelText(): String = "%.1f".format(tile.internal.powerPerTick) + " DE/t"
}

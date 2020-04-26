package com.itszuvalex.femtocraft.power.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabNetwork}
import com.itszuvalex.femtocraft.industry.gui.GuiPowerMeter
import com.itszuvalex.femtocraft.power.container.ContainerCrystalStorageArray
import com.itszuvalex.femtocraft.power.tile.TileCrystalStorageArray
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.storage.IItemStorage
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}

class GuiCrystalStorageArray(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileCrystalStorageArray)
  extends FemtoGuiBase(tile, new ContainerCrystalStorageArray(player, inv, tile, false)) {
  GuiTabNetwork.addToGuiTabBar(tabBar, tile)

  val storage: IItemStorage = tile.getModule(ItszuLibModules.ITEM_STORAGE, null)
  storage.indices.foreach(i =>
                            addGuiAndSync(storage, i, 61 + 18 * (i % 3), 23 + 18 * (i / 3))
                          )
  addPlayerInventorySlots(inv)

  val powerMeter = new GuiPowerMeter(6, 22, tile.getModule(ManagerModules.TILE_WIRELESS_POWER_STORAGE_NODE, null).battery, tile.getModule(ItszuLibModules.COLORABLE, null).toInt)
  add(powerMeter)

  override def GuiID: Int = GuiIDs.TileCrystalStorageArrayID
}

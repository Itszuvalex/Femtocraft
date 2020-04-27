package com.itszuvalex.femtocraft.power.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabWirelessPowerNetwork}
import com.itszuvalex.femtocraft.power.container.ContainerCrystalMount
import com.itszuvalex.femtocraft.power.tile.TileCrystalMount
import com.itszuvalex.itszulib.api.ItszuLibModules
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
class GuiCrystalMount(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileCrystalMount)
  extends FemtoGuiBase(tile, new ContainerCrystalMount(player, inv, tile, false)) {
  GuiTabWirelessPowerNetwork.addToGuiTabBar(tabBar, tile)

  addGuiAndSync(tile.getModule(ItszuLibModules.ITEM_STORAGE, null), 0, 79, 33)
  addPlayerInventorySlots(inv)

  override def GuiID: Int = GuiIDs.TileCrystalMountGuiID
}

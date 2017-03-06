package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.client.FemtoGuiBase
import com.itszuvalex.femtocraft.power.container.ContainerPowerNetwork
import com.itszuvalex.itszulib.core.TileEntityBase

/**
  * Created by Christopher Harris (Itszuvalex) on 1/27/17.
  */

class GuiSidedInventoryConfig(tile: TileEntityBase) extends FemtoGuiBase(tile, new ContainerPowerNetwork(tile, false)) {
  override def GuiID: Int = GuiIDs.TileSidedInventoryConfigID
}

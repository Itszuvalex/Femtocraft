package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.gui.GuiPanelTexture

/**
  * Created by Chris on 1/29/2017.
  */
object GuiTabNetwork {
  val NetworkTexLoc = Resources.TexGui("tabnetwork.png")

  def addToGuiTabBar(bar: GuiTabBar, tile: TileEntityBase): Unit = {
    bar.addTab(new GuiTabNetwork("Network", tile, bar.getActiveGuiID))
  }
}

class GuiTabNetwork(text: String,
  tile: TileEntityBase,
  activeGuiID: () => Int)
  extends GuiTab(text, new GuiPanelTexture(2, 2, 18, 18, GuiTabNetwork.NetworkTexLoc), tile, GuiIDs.TilePowerNetworkID, activeGuiID)

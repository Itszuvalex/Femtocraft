package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.gui.GuiPanelTexture

/**
  * Created by Chris on 1/29/2017.
  */
object GuiTabWirelessPowerNetwork {
  val NetworkTexLoc = Resources.TexGui("tabnetwork.png")

  def addToGuiTabBar(bar: GuiTabBar, tile: ITileEntity): Unit = {
    bar.addTab(new GuiTabWirelessPowerNetwork("Network", tile, bar.getActiveGuiID))
  }
}

class GuiTabWirelessPowerNetwork(text: String,
                                 tile: ITileEntity,
                                 activeGuiID: () => Int)
  extends GuiTab(text, new GuiPanelTexture(2, 2, 18, 18, GuiTabWirelessPowerNetwork.NetworkTexLoc), tile, GuiIDs.TileWirelessPowerNetworkID, activeGuiID)

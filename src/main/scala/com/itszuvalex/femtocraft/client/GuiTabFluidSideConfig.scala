package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.gui.GuiPanelTexture

object GuiTabFluidSideConfig {
  val SideConfigTexLoc = Resources.TexGui("tabfluid.png")

  def addToGuiTabBar(bar: GuiTabBar, tile: ITileEntity): Unit = {
    bar.addTab(new GuiTabFluidSideConfig("Fluid Config", tile, bar.getActiveGuiID))
  }
}

class GuiTabFluidSideConfig(text: String,
                            tile: ITileEntity,
                            activeGuiID: () => Int)
  extends GuiTab(text, new GuiPanelTexture(2, 2, 18, 18, GuiTabFluidSideConfig.SideConfigTexLoc), tile, GuiIDs.TileSidedFluidConfigID, activeGuiID)
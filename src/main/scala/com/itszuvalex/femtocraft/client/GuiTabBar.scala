package com.itszuvalex.femtocraft.client

import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.gui.{GuiFlowLayout, GuiPanel}

/**
  * Created by Christopher Harris (Itszuvalex) on 1/26/17.
  */
object GuiTabBar {
  val WIDTH = GuiTab.WIDTH
}

class GuiTabBar(anchorX: Int, anchorY: Int, height: Int, currentGuiID: Int, tile: TileEntityBase) extends
  GuiFlowLayout(anchorX, anchorY, GuiTabBar.WIDTH, height) {
  primaryFlow = GuiFlowLayout.FlowDirection.Vertical

  def addTab(name: String, iconRender: GuiPanel, guiID: Int): GuiPanel = addTab(new GuiTab(name, iconRender, tile, guiID, getActiveGuiID))

  def addTab(tab: GuiTab): GuiPanel = add(tab)

  def getActiveGuiID(): Int = currentGuiID

}

package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.MessageOpenGui
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.gui.{GuiButton, GuiPanel}

import scala.collection.mutable.ListBuffer

/**
  * Created by Christopher Harris (Itszuvalex) on 1/26/17.
  */
object GuiTab {
  val WIDTH  = 20
  val HEIGHT = 20
}

class GuiTab(text: String,
  var iconRender: GuiPanel,
  var tile: TileEntityBase,
  var guiID: Int,
  var activeGuid: () => Int) extends GuiButton(0, 0, GuiTab.WIDTH, GuiTab.HEIGHT, "") {
  iconRender.anchorX = (panelWidth - iconRender.panelWidth) / 2
  iconRender.anchorY = (panelHeight - iconRender.panelHeight) / 2
  add(iconRender)

  override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
    super.addTooltip(mouseX, mouseY, tooltip)
    if (isMousedOver) {
      tooltip += text
    }
  }

  override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
    val ret = super.onMouseClick(mouseX, mouseY, button)
    if (ret && activeGuid() != guiID) {
      FemtoPacketHandler.INSTANCE.sendToServer(new MessageOpenGui(tile, guiID))
    }
    ret
  }
}

package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.itszulib.gui.GuiPanelTexture
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.mutable.ListBuffer

@SideOnly(Side.CLIENT)
object GuiIcons {
  def guiIconBatteryGreen(anchorX: Int, anchorY: Int, tooltipFunc: (ListBuffer[String]) => Unit = null): GuiPanelTexture
  = new GuiPanelTexture(anchorX, anchorY, 8, 16, Resources.TexGui("iconpowerproducer.png")) {
    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      super.addTooltip(mouseX, mouseY, tooltip)
      if (isMousedOver && tooltipFunc != null)
        tooltipFunc(tooltip)
    }
  }

  def guiIconBatteryYellow(anchorX: Int, anchorY: Int, tooltipFunc: (ListBuffer[String]) => Unit = null): GuiPanelTexture
  = new GuiPanelTexture(anchorX, anchorY, 8, 16, Resources.TexGui("iconpowerstorage.png")) {
    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      super.addTooltip(mouseX, mouseY, tooltip)
      if (isMousedOver && tooltipFunc != null)
        tooltipFunc(tooltip)
    }
  }

  def guiIconBatteryRed(anchorX: Int, anchorY: Int, tooltipFunc: (ListBuffer[String]) => Unit = null): GuiPanelTexture
  = new GuiPanelTexture(anchorX, anchorY, 8, 16, Resources.TexGui("iconpowerconsumer.png")) {
    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      super.addTooltip(mouseX, mouseY, tooltip)
      if (isMousedOver && tooltipFunc != null)
        tooltipFunc(tooltip)
    }
  }

  def guiIconBatteryDelta(anchorX: Int, anchorY: Int, tooltipFunc: (ListBuffer[String]) => Unit = null): GuiPanelTexture
  = new GuiPanelTexture(anchorX, anchorY, 8, 16, Resources.TexGui("iconpowerdelta.png")) {
    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      super.addTooltip(mouseX, mouseY, tooltip)
      if (isMousedOver && tooltipFunc != null)
        tooltipFunc(tooltip)
    }
  }

  def guiIconBatteryStorageYellow(anchorX: Int, anchorY: Int, tooltipFunc: (ListBuffer[String]) => Unit = null): GuiPanelTexture
  = new GuiPanelTexture(anchorX, anchorY, 16, 16, Resources.TexGui("iconpowerdedicatedstorage.png")) {
    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      super.addTooltip(mouseX, mouseY, tooltip)
      if (isMousedOver && tooltipFunc != null)
        tooltipFunc(tooltip)
    }
  }

  def guiIconBatteryStorageAll(anchorX: Int, anchorY: Int, tooltipFunc: (ListBuffer[String]) => Unit = null): GuiPanelTexture
  = new GuiPanelTexture(anchorX, anchorY, 16, 16, Resources.TexGui("iconpowertotalstorage.png")) {
    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      super.addTooltip(mouseX, mouseY, tooltip)
      if (isMousedOver && tooltipFunc != null)
        tooltipFunc(tooltip)
    }
  }
}

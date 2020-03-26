package com.itszuvalex.femtocraft.power.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.power.PowerNetwork
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiIcons}
import com.itszuvalex.femtocraft.power.container.ContainerPowerNetwork
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.gui.{GuiLabel, GuiSpacingPanel}
import com.itszuvalex.itszulib.util.ChatHelper
import net.minecraft.client.Minecraft

/**
  * Created by Christopher Harris (Itszuvalex) on 1/27/17.
  */

class GuiPowerNetwork(tile: ITileEntity) extends FemtoGuiBase(tile, new ContainerPowerNetwork(tile, false)) {
  val network            = inventorySlots.asInstanceOf[ContainerPowerNetwork]
  val numbersPanel       = new GuiSpacingPanel(5, 5, panelWidth, HEIGHT)
  val nodeCountString    = "Node Count"
  val nodeCountLabel     = new GuiLabel((panelWidth - fontRendererActual.getStringWidth(nodeCountString)) / 2, 0, panelWidth, fontRendererActual.FONT_HEIGHT, () => nodeCountString)
  val producersCountIcon = GuiIcons.guiIconBatteryGreen(0, HEIGHT / 2 - 4, _ += "Producers")
  val producersLabel     = new GuiLabel(0 + 9, HEIGHT / 2, HORIZ / 2, HEIGHT / 2, () => s"${network.producerCount}")
  val storageCountIcon   = GuiIcons.guiIconBatteryYellow(HORIZ, HEIGHT / 2 - 4, _ += "Storage")
  val storageCountLabel  = new GuiLabel(HORIZ + 9, HEIGHT / 2, HORIZ / 2, HEIGHT / 2, () => s"${network.storageCount}")
  val consumersCountIcon = GuiIcons.guiIconBatteryRed(2 * HORIZ, HEIGHT / 2 - 4, _ += "Consumers")
  val consumersLabel     = new GuiLabel(2 * HORIZ + 9, HEIGHT / 2, HORIZ / 2, HEIGHT / 2, () => s"${network.consumerCount}")
  val changePanel        = new GuiSpacingPanel(5, HEIGHT + 15, panelWidth, HEIGHT)
  val nodeChangeString   = "Node Change"
  val nodeAmountLabel    = new GuiLabel((panelWidth - fontRendererActual.getStringWidth(nodeChangeString)) / 2, 0, panelWidth, fontRendererActual.FONT_HEIGHT, () => nodeChangeString)
  val producerAmountIcon = GuiIcons.guiIconBatteryGreen(0, HEIGHT / 2 - 4, _ += "Produced")
  numbersPanel.add(nodeCountLabel, producersCountIcon, producersLabel, storageCountIcon, storageCountLabel, consumersCountIcon, consumersLabel)
  val producerAmountLabel = new GuiLabel(0 + 9, HEIGHT / 2, HORIZ, HEIGHT / 2, () => f"${network.currentGen}%,.1f")
  val storageAmountIcon   = GuiIcons.guiIconBatteryYellow(HORIZ, HEIGHT / 2 - 4, _ += "Stored")
  val storageDeltaLabel   = new GuiLabel(HORIZ + 9, HEIGHT / 2, HORIZ, HEIGHT / 2, () => f"${network.storageDelta}%,.1f")
  val consumersAmountIcon = GuiIcons.guiIconBatteryRed(2 * HORIZ, HEIGHT / 2 - 4, _ += "Consumed")
  val consumerAmountLabel = new GuiLabel(2 * HORIZ + 9, HEIGHT / 2, HORIZ, HEIGHT / 2, () => f"${network.currentDrain}%,.1f")
  val networkPanel        = new GuiSpacingPanel(5, 2 * HEIGHT + 25, panelWidth, HEIGHT)
  val networkChangeString = "Network Change"
  val networkChangeLabel  = new GuiLabel((panelWidth - fontRendererActual.getStringWidth(networkChangeString)) / 2, 0, panelWidth, fontRendererActual.FONT_HEIGHT, () => networkChangeString)
  val networkChangeIcon   = GuiIcons.guiIconBatteryDelta((panelWidth - 8) / 2, HEIGHT / 2, _ += "Network Change")
  changePanel.add(nodeAmountLabel, producerAmountIcon, producerAmountLabel, storageAmountIcon, storageDeltaLabel, consumersAmountIcon, consumerAmountLabel)
  val lastTickString       = f"Last Tick: ${network.lastNetworkDelta}%,.1f"
  val networkLastTickLabel = new GuiLabel(0, HEIGHT / 2, (panelWidth - 8) / 2, HEIGHT / 2, () => lastTickString)
  val networkAverageLabel  = new GuiLabel(2 * HORIZ, HEIGHT / 2, (panelWidth - 8) / 2, HEIGHT / 2, () => f"${PowerNetwork.TICKS_TO_AVERAGE_POWER_OVER} Ticks: ${network.networkAvg}%,.1f")
  val storagePanel         = new GuiSpacingPanel(5, 3 * HEIGHT + 35, panelWidth, (5 * HEIGHT) / 2)
  val storageString        = "Storage"
  val storageLabel         = new GuiLabel((panelWidth - fontRendererActual.getStringWidth(storageString)) / 2, 0, panelWidth, fontRendererActual.FONT_HEIGHT, () => storageString)
  val dedicatedStorageIcon = GuiIcons.guiIconBatteryStorageYellow(0, HEIGHT / 2 - 4, _ += "Dedicated Storage")

  networkPanel.add(networkChangeLabel, networkLastTickLabel, networkChangeIcon, networkAverageLabel)
  val storageAmountLabel      = new GuiLabel(17, HEIGHT / 2, panelWidth - 17, HEIGHT, () => f"${network.networkStored}%,.1f/${network.networkStorage}%,.1f DE")
  val totalStorageIcon        = GuiIcons.guiIconBatteryStorageAll(0, ((3 * HEIGHT) / 2) - 4, _ += "Total Storage")
  val totalStorageAmountLabel = new GuiLabel(17, (3 * HEIGHT) / 2, panelWidth - 17, HEIGHT, () => f"${network.networkTotalStored}%,.1f/${network.networkTotalStorage}%,.1f DE")
  val overviewPanel           = new GuiSpacingPanel(5, (9 * HEIGHT) / 2 + 45, panelWidth, HEIGHT / 2)
  val storageStringFunc       = () => if (network.networkAvg > 0) f"Filled in: ${ChatHelper.green(f"${(network.networkTotalStorage - network.networkTotalStored) / network.networkAvg}%,.0f")} ticks."
  else f"Empty in: ${ChatHelper.red(f"${network.networkTotalStored / Math.abs(network.networkAvg)}%,.0f")} ticks."
  val overviewLabel           = new GuiLabel((panelWidth - fontRendererActual.getStringWidth(storageStringFunc())) / 2, 0, panelWidth, fontRendererActual.FONT_HEIGHT, storageStringFunc)

  def HORIZ: Int = panelWidth / 3 - 10

  storagePanel.add(storageLabel, dedicatedStorageIcon, storageAmountLabel, totalStorageIcon, totalStorageAmountLabel)

  def HEIGHT: Int = panelHeight / 5 - 10

  override def GuiID: Int = GuiIDs.TilePowerNetworkID

  def fontRendererActual = Minecraft.getMinecraft.fontRenderer

  overviewPanel.add(overviewLabel)


  add(numbersPanel, changePanel, networkPanel, storagePanel, overviewPanel)

}

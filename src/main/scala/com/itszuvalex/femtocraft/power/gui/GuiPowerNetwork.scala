package com.itszuvalex.femtocraft.power.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.client.FemtoGuiBase
import com.itszuvalex.femtocraft.power.container.ContainerPowerNetwork
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.gui.{GuiLabel, GuiSpacingPanel}

/**
  * Created by Christopher Harris (Itszuvalex) on 1/27/17.
  */

class GuiPowerNetwork(tile: TileEntityBase) extends FemtoGuiBase(tile, new ContainerPowerNetwork(tile, false)) {
  def HORIZ: Int = panelWidth / 3 - 10

  def HEIGHT: Int = panelHeight / 5 - 10

  override def GuiID: Int = GuiIDs.TilePowerNetworkID

  val numbersPanel   = new GuiSpacingPanel(5, 5, panelWidth, HEIGHT)
  val producersLabel = new GuiLabel(0, 0, HORIZ, HEIGHT, () => s"Producers: ${inventorySlots.asInstanceOf[ContainerPowerNetwork].producerCount}")
  val storageLabel   = new GuiLabel(HORIZ, 0, HORIZ, HEIGHT, () => s"Storage: ${inventorySlots.asInstanceOf[ContainerPowerNetwork].storageCount}")
  val consumersLabel = new GuiLabel(2 * HORIZ, 0, HORIZ, HEIGHT, () => s"Consumers: ${inventorySlots.asInstanceOf[ContainerPowerNetwork].consumerCount}")
  numbersPanel.add(producersLabel, storageLabel, consumersLabel)

  val changePanel         = new GuiSpacingPanel(5, HEIGHT + 15, panelWidth, HEIGHT)
  val producerAmountLabel = new GuiLabel(0, 0, HORIZ, HEIGHT, () => f"Produced: ${inventorySlots.asInstanceOf[ContainerPowerNetwork].currentGen}%,.1f")
  val storageDeltaLabel   = new GuiLabel(HORIZ, 0, HORIZ, HEIGHT, () => f"Storage Change: ${inventorySlots.asInstanceOf[ContainerPowerNetwork].storageDelta}%,.1f")
  val consumerAmountLabel = new GuiLabel(2 * HORIZ, 0, HORIZ, HEIGHT, () => f"Consumed: ${inventorySlots.asInstanceOf[ContainerPowerNetwork].currentDrain}%,.1f")
  changePanel.add(producerAmountLabel, storageDeltaLabel, consumerAmountLabel)

  val networkPanel         = new GuiSpacingPanel(5, 2 * HEIGHT + 25, panelWidth, HEIGHT)
  val networkLastTickLabel = new GuiLabel(0, 0, HORIZ, HEIGHT, () => f"Network Change: ${inventorySlots.asInstanceOf[ContainerPowerNetwork].lastNetworkDelta}%,.1f")
  val networkAverageLabel  = new GuiLabel(2 * HORIZ, 0, HORIZ, HEIGHT, () => f"Network Avg: ${inventorySlots.asInstanceOf[ContainerPowerNetwork].networkAvg}%,.1f")
  networkPanel.add(networkLastTickLabel, networkAverageLabel)

  val storagePanel       = new GuiSpacingPanel(5, 3 * HEIGHT + 35, panelWidth, HEIGHT)
  val storageAmountLabel = new GuiLabel(0, 0, 200, HEIGHT, () => f"Storage: ${inventorySlots.asInstanceOf[ContainerPowerNetwork].networkStored}%,.1f/${inventorySlots.asInstanceOf[ContainerPowerNetwork].networkStorage}%,.1f DE")
  storagePanel.add(storageAmountLabel)

  add(numbersPanel, changePanel, networkPanel, storagePanel)

}

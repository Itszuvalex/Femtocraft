package com.itszuvalex.femtocraft.power.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabWirelessPowerNetwork}
import com.itszuvalex.femtocraft.industry.gui.GuiPowerMeter
import com.itszuvalex.femtocraft.power.container.ContainerCrystalChargingArray
import com.itszuvalex.femtocraft.power.tile.TileCrystalChargingArray
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.gui.GuiLabel
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}

class GuiCrystalChargingArray(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileCrystalChargingArray)
  extends FemtoGuiBase(tile, new ContainerCrystalChargingArray(player, inv, tile, false)) {
  GuiTabWirelessPowerNetwork.addToGuiTabBar(tabBar, tile)

  tile.storage.indices.foreach(i =>
    addGuiAndSync(tile.storage, i, 61 + 18 * (i % 3), 23 + 18 * (i / 3))
  )
  addPlayerInventorySlots(inv)

  val powerMeter   = new GuiPowerMeter(6, 22, tile.getCapability(Capabilities.TILE_WIRELESS_POWER_STORAGE_NODE, null).battery, tile.getCapability(ItszuLibCapabilities.COLORABLE, null).toInt)
  val powerReading = new GuiLabel(6, 14, 80, Minecraft.getMinecraft.fontRenderer.FONT_HEIGHT, labelText)
  add(powerReading, powerMeter)

  override def GuiID: Int = GuiIDs.TileCrystalChargingArrayID

  def labelText(): String = "%.1f".format(tile.powerPerTick) + " DE/t"
}

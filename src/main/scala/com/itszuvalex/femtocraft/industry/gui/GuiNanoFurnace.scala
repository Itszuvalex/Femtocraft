package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabNetwork}
import com.itszuvalex.femtocraft.industry.container.ContainerNanoFurnace
import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace
import com.itszuvalex.itszulib.gui.{GuiLabel, GuiProgress}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing

/**
  * Created by Alex on 18.08.2016.
  */
class GuiNanoFurnace(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileNanoFurnace) extends FemtoGuiBase(tile, new ContainerNanoFurnace(player, inv, tile, false)) {
  GuiTabNetwork.addToGuiTabBar(tabBar, tile)

  fontRendererObj = Minecraft.getMinecraft.fontRendererObj

  addGuiAndSync(tile.storage, 0, 44, 23)
  addGuiAndSync(tile.storage, 1, 85, 23)

  addPlayerInventorySlots(inv)

  //TODO: Make actual "machine color"
  var color: Color = tile.getCapability(Capabilities.COLORABLE, EnumFacing.UP)

  val progressBar = new GuiProgress(44 + 18, 23 + 7, 85 - (44 + 18), 4, () => (tile.getProgress / tile.getProgressMax).toFloat)
  progressBar.colorProgress = color.toInt
  add(progressBar)

  val nameLabel  = new GuiLabel(20, 12, fontRendererObj.getStringWidth("Nano Furnace"), fontRendererObj.FONT_HEIGHT, () => "Nano Furnace")
  val powerMeter = new GuiPowerMeter(6, 22, tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery, color.toInt)

  val elems = List(nameLabel, powerMeter)
  add(elems: _*)

  //  elems.foreach(e => e.setShouldRender(false))
  override def GuiID: Int = GuiIDs.TileFurnaceGuiID
}

package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.FemtoGuiBase
import com.itszuvalex.femtocraft.industry.container.ContainerDemolisher
import com.itszuvalex.femtocraft.industry.tile.TileDemolisher
import com.itszuvalex.itszulib.gui.{GuiLabel, GuiProgress}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing

class GuiDemolisher(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileDemolisher) extends FemtoGuiBase(tile, new ContainerDemolisher(player, inv, tile, false)) {

  fontRendererObj = Minecraft.getMinecraft.fontRendererObj
  xSize = 183
  ySize = 161

  addGuiAndSync(tile.storage, 0, 44, 23)
  addGuiAndSync(tile.storage, 1, 85, 23)

  addPlayerInventorySlots(inv)

  //TODO: Make actual "machine color"
  var color: Color = tile.getCapability(Capabilities.COLORABLE, EnumFacing.UP)

  val progressBar = new GuiProgress(44 + 18, 23 + 7, 85 - (44 + 18), 4, () => (tile.getProgress / tile.getProgressMax).toFloat)
  progressBar.colorProgress = color.toInt
  add(progressBar)

  val nameLabel  = new GuiLabel(20, 12, fontRendererObj.getStringWidth("Demolisher"), fontRendererObj.FONT_HEIGHT, () => "Demolisher")
  val powerMeter = new GuiPowerMeter(6, 22, tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery, color.toInt)

  val elems = List(nameLabel, powerMeter)
  add(elems: _*)
  //  elems.foreach(e => e.setShouldRender(false))
}

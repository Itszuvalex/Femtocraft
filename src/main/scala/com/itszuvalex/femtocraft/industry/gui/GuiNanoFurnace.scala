package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.FemtoGuiBase
import com.itszuvalex.femtocraft.industry.container.ContainerNanoFurnace
import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import com.itszuvalex.itszulib.gui.{GuiItemStack, GuiLabel, GuiProgress}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11

/**
  * Created by Alex on 18.08.2016.
  */
object GuiNanoFurnace {
  val texture      = Resources.TexGui("guinanofurnace.png")
  val colorTexture = Resources.TexGui("guinanofurnacecolor.png")
}

class GuiNanoFurnace(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileNanoFurnace) extends FemtoGuiBase(tile, new ContainerNanoFurnace(player, inv, tile, false)) {

  fontRendererObj = Minecraft.getMinecraft.fontRendererObj
  xSize = 183
  ySize = 161

  addGuiAndSync(tile.storage, 0, 44, 23)
  addGuiAndSync(tile.storage, 1, 85, 23)

  addPlayerInventorySlots(inv, 4, 75)

  //TODO: Make actual "machine color"
  var color: Color = tile.getCapability(Capabilities.COLORABLE, EnumFacing.UP)

  val progressBar = new GuiProgress(44 + 18, 23 + 7, 85 - (44 + 18), 4, () => (tile.getProgress / tile.getProgressMax).toFloat)
  progressBar.colorProgress = color.toInt
  add(progressBar)

  val nameLabel  = new GuiLabel(20, 12, fontRendererObj.getStringWidth("Nano Furnace"), fontRendererObj.FONT_HEIGHT, () => "Nano Furnace")
//  val inputSlot  = new GuiItemStack(44, 23) {override def itemStack = IItemStack.Empty}
  //TODO: IItemStack.Empty - needs ItszuLib GuiItemStack change
//  val outputSlot = new GuiItemStack(85, 23) {override def itemStack = IItemStack.Empty}
  // TODO: IItemStack.Empty
  val powerMeter = new GuiPowerMeter(6, 22, tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery, color.toInt)

  val elems = List(nameLabel, powerMeter)
  add(elems: _*)
  //  elems.foreach(e => e.setShouldRender(false))
}

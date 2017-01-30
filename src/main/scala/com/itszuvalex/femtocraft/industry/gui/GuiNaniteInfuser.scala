package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabNetwork}
import com.itszuvalex.femtocraft.industry.container.ContainerNaniteInfuser
import com.itszuvalex.femtocraft.industry.tile.TileNaniteInfuser
import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import com.itszuvalex.itszulib.gui.{GuiItemStack, GuiLabel}
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing

object GuiNaniteInfuser {
  //TODO; Temporarily using the nano furnace textures.
  val texture      = Resources.TexGui("guinanofurnace.png")
  val colorTexture = Resources.TexGui("guinanofurnacecolor.png")
}

class GuiNaniteInfuser(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileNaniteInfuser) extends FemtoGuiBase(tile, new ContainerNaniteInfuser(player, inv, tile)) {
  GuiTabNetwork.addToGuiTabBar(tabBar, tile)

  fontRendererObj = Minecraft.getMinecraft.fontRendererObj
  xSize = 183
  ySize = 161

  var color = tile.getCapability(Capabilities.COLORABLE, EnumFacing.UP)

  val nameLabel  = new GuiLabel(20, 12, fontRendererObj.getStringWidth("Nanite Infuser"), fontRendererObj.FONT_HEIGHT, () => "Nanite Infuser")
  val inputSlot  = new GuiItemStack(44, 23) {override def itemStack = IItemStack.Empty}
  val outputSlot = new GuiItemStack(85, 23) {override def itemStack = IItemStack.Empty}
  val powerMeter = new GuiPowerMeter(6, 22, tile.getCapability(Capabilities.POWER_STORAGE, EnumFacing.UP), color.toInt)

  val elems = List(nameLabel, inputSlot, outputSlot, powerMeter)
  add(elems: _*)

  override def GuiID: Int = GuiIDs.TileNaniteInfuserID
}

package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.container.ContainerNanoFurnace
import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import com.itszuvalex.itszulib.container.ContainerBase
import com.itszuvalex.itszulib.container.sync.SyncItemStorageItemStack
import com.itszuvalex.itszulib.gui.{GuiBase, GuiIItemStorageSlot, GuiItemStack, GuiLabel}
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

class GuiNanoFurnace(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileNanoFurnace) extends GuiBase(new ContainerNanoFurnace(player, inv, tile, false)) {

  fontRendererObj = Minecraft.getMinecraft.fontRendererObj
  xSize = 183
  ySize = 161

  {
    var gui = new GuiIItemStorageSlot(44, 23, tile.storage, 0)
    gui.sync = new SyncItemStorageItemStack(tile.storage, 0)
    this.add(gui)
    inventorySlots.asInstanceOf[ContainerBase].addSync(gui.sync)

    gui = new GuiIItemStorageSlot(85, 23, tile.storage, 1)
    gui.sync = new SyncItemStorageItemStack(tile.storage, 1)
    this.add(gui)
    inventorySlots.asInstanceOf[ContainerBase].addSync(gui.sync)
  }

  addPlayerInventorySlots(inv, 4, 75)

  //TODO: Make actual "machine color"
  var color: Color = tile.getCapability(Capabilities.COLORABLE, EnumFacing.UP)

  val nameLabel  = new GuiLabel(20, 12, fontRendererObj.getStringWidth("Nano Furnace"), fontRendererObj.FONT_HEIGHT, "Nano Furnace")
  val inputSlot  = new GuiItemStack(44, 23) {override def itemStack = IItemStack.Empty}
  //TODO: IItemStack.Empty - needs ItszuLib GuiItemStack change
  val outputSlot = new GuiItemStack(85, 23) {override def itemStack = IItemStack.Empty}
  // TODO: IItemStack.Empty
  val powerMeter = new GuiPowerMeter(6, 22, tile.getCapability(Capabilities.POWER_STORAGE, EnumFacing.UP), color.toInt)

  val elems = List(nameLabel, inputSlot, outputSlot, powerMeter)
  add(elems: _*)
  elems.foreach(e => e.setShouldRender(false))

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glDisable(GL11.GL_LIGHTING)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiNanoFurnace.texture)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 2, xSize, ySize)

    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiNanoFurnace.colorTexture)
    GL11.glColor4f((color.red & 255) / 255f, (color.green & 255) / 255f, (color.blue & 255) / 255f, 1)
    drawTexturedModalRect(k + 67, l + 48, 0, 0, 106, 107)

    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiNanoFurnace.texture)
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    val prog = (tile.getProgress / tile.getProgressMax * 13).toInt
    drawTexturedModalRect(k + 67, l + 26 + 13 - prog, 183, 13 - prog, 13, prog)

    GL11.glScaled(.5, .5, .5)
    nameLabel.render(2 * (anchorX + nameLabel.anchorX), 2 * (anchorY + nameLabel.anchorY), mouseX - anchorX - nameLabel.anchorX, mouseY - anchorY - nameLabel.anchorY, partialTicks)
    GL11.glScaled(2, 2, 2)
    inputSlot.render(anchorX + inputSlot.anchorX, anchorY + inputSlot.anchorY, mouseX - anchorX - inputSlot.anchorX, mouseY - anchorY - inputSlot.anchorY, partialTicks)
    outputSlot.render(anchorX + outputSlot.anchorX, anchorY + outputSlot.anchorY, mouseX - anchorX - outputSlot.anchorX, mouseY - anchorY - outputSlot.anchorY, partialTicks)
    powerMeter.render(anchorX + powerMeter.anchorX, anchorY + powerMeter.anchorY, mouseX - anchorX - powerMeter.anchorX, mouseY - anchorY - powerMeter.anchorY, partialTicks)

    super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY)
  }

}

package com.itszuvalex.femtocraft.power.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.gui.GuiPowerMeter
import com.itszuvalex.femtocraft.power.container.ContainerCrystalHeatExchanger
import com.itszuvalex.femtocraft.power.tile.TileCrystalHeatExchanger
import com.itszuvalex.itszulib.gui.{GuiBase, GuiLabel, GuiProgress}
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import org.lwjgl.opengl.GL11


object GuiCrystalHeatExchanger {
  val TEXTURE_LOC = Resources.TexGui("guicrystalheatexchanger.png")
}

class GuiCrystalHeatExchanger(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileCrystalHeatExchanger)
  extends GuiBase(new ContainerCrystalHeatExchanger(player, inv, tile, false)) {
  tile.storage.indices.foreach(i =>
                                 addGuiAndSync(tile.storage, i, 61 + 18 * (i % 3), 23 + 18 * (i / 3))
                              )
  addPlayerInventorySlots(inv)

  val progressGui = new GuiProgress(58, 23, 3, 18, () => tile.getBurnTime.toFloat / tile.getBurnMax.toFloat, direction = GuiProgress.BottomUp)
  progressGui.colorProgress = tile.getCapability(Capabilities.COLORABLE, null).toInt
  add(progressGui)

  val powerMeter   = new GuiPowerMeter(6, 22, tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery, tile.getCapability(Capabilities.COLORABLE, null).toInt)
  val powerReading = new GuiLabel(6, 14, 80, Minecraft.getMinecraft.fontRendererObj.FONT_HEIGHT, labelText)
  add(powerReading, powerMeter)

  override def drawGuiContainerBackgroundLayer(p_146976_1_ : Float, p_146976_2_ : Int, p_146976_3_ : Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiCrystalHeatExchanger.TEXTURE_LOC)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 0, xSize, ySize)

    super.drawGuiContainerBackgroundLayer(p_146976_1_, p_146976_2_, p_146976_3_)
  }

  def labelText(): String = "%.1f".format(tile.powerPerTick) + " DE/t"
}

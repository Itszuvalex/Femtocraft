package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.industry.container.ContainerMaterialProcessor
import com.itszuvalex.femtocraft.industry.tile.TileMaterialProcessor
import com.itszuvalex.femtocraft.util.StringUtil
import com.itszuvalex.itszulib.gui.GuiBase
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import org.lwjgl.opengl.GL11

object GuiMaterialProcessor {
  val texture = Resources.TexGui("GuiMaterialProcessor.png")
}

@SideOnly(Side.CLIENT) class GuiMaterialProcessor(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileMaterialProcessor) extends
  GuiBase(new ContainerMaterialProcessor(player, inv, tile)) {


  override def drawScreen(par1: Int, par2: Int, par3: Float) {
    super.drawScreen(par1, par2, par3)
    val text = StringUtil.formatPowerString(tile.getPowerCurrent, tile.getPowerMax)
    if (isPointInRegion(18, 12, 16, 60, par1, par2)) {
      drawCreativeTabHoveringText(text, par1, par2)
    }
  }

  /**
    * Draw the foreground layer for the GuiContainer (everything in front of the items)
    */
  protected override def drawGuiContainerForegroundLayer(par1: Int, par2: Int) {
    //    fontRendererObj.drawString(s, xSize / 2 - fontRendererObj.getStringWidth(s) / 2, 6, Color(0, 255.toByte, 255.toByte, 255.toByte).toInt)
    //    fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, Color(0, 255.toByte, 255.toByte, 255.toByte).toInt)
  }

  /**
    * Draw the background layer for the GuiContainer (everything behind the items)
    */
  protected def drawGuiContainerBackgroundLayer(par1: Float, par2: Int, par3: Int) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiMaterialProcessor.texture)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 0, xSize, ySize)
    var i1 = 0
    i1 = 0 /*furnaceInventory.getCookProgressScaled(38)*/
    drawTexturedModalRect(k + 73, l + 34, 176, 13, i1, 18)
    i1 = 0 /*(furnaceInventory.currentPower * 60) / furnaceInventory.getMaxPower*/
    drawTexturedModalRect(k + 18, l + 12 + (60 - i1), 176, 32 + (60 - i1), 16 + (60 - i1), 60)
  }

}

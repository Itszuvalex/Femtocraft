package com.itszuvalex.femtocraft.nanite.gui

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.nanite.container.ContainerNaniteHive
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteHiveSmall
import com.itszuvalex.itszulib.gui.GuiBase
import com.itszuvalex.itszulib.util.Color
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.{ResourceLocation, StatCollector}
import org.lwjgl.opengl.GL11

/**
 * Created by Christopher on 9/1/2015.
 */
@SideOnly(Side.CLIENT) object GuiNaniteHive {
  val texture = new ResourceLocation(Femtocraft.ID.toLowerCase, "textures/guis/GuiNaniteHive_small.png")
  val WIDTH   = 226
  val HEIGHT  = 166
}

@SideOnly(Side.CLIENT) class GuiNaniteHive(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileNaniteHiveSmall) extends
GuiBase(new ContainerNaniteHive(player, inv, tile)) {
  xSize = GuiNaniteHive.WIDTH
  ySize = GuiNaniteHive.HEIGHT

  /**
   * Draw the foreground layer for the GuiContainer (everything in front of the items)
   */
  protected override def drawGuiContainerForegroundLayer(par1: Int, par2: Int) {
    val s = "Small Nanite Hive"
    fontRendererObj.drawString(s, xSize / 2 - fontRendererObj.getStringWidth(s) / 2, 6, Color(0, 255.toByte, 255.toByte, 255.toByte).toInt)
    fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 30, ySize - 96 + 4, Color(0, 255.toByte, 255.toByte, 255.toByte).toInt)
  }

  /**
   * Draw the background layer for the GuiContainer (everything behind the items)
   */
  protected def drawGuiContainerBackgroundLayer(par1: Float, par2: Int, par3: Int) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiNaniteHive.texture)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 0, xSize, ySize)
    //    var i1 = 0
    //    i1 = 0 /*furnaceInventory.getCookProgressScaled(38)*/
    //    drawTexturedModalRect(k + 73, l + 34, 176, 13, i1, 18)
    //    i1 = 0 /*(furnaceInventory.currentPower * 60) / furnaceInventory.getMaxPower*/
    //    drawTexturedModalRect(k + 18, l + 12 + (60 - i1), 176, 32 + (60 - i1), 16 + (60 - i1), 60)
  }

}

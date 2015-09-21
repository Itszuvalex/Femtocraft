package com.itszuvalex.femtocraft.core.Industry.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.core.Industry.container.ContainerFrame
import com.itszuvalex.femtocraft.core.Industry.tile.TileFrame
import com.itszuvalex.itszulib.gui.{GuiBase, GuiItemStack}
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import org.lwjgl.opengl.GL11

/**
 * Created by Christopher on 9/21/2015.
 */
object GuiFrame {
  val texture = Resources.TexGui("GuiInventoryBase.png")
}

class GuiFrame(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileFrame) extends GuiBase(new ContainerFrame(player, inv, tile)) {

//  add(
//       (0 until 9).map(i => new GuiItemStack(7 + 18 * i, 62, null)): _*
//     )


  override def drawGuiContainerBackgroundLayer(p_146976_1_ : Float, p_146976_2_ : Int, p_146976_3_ : Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiFrame.texture)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 0, xSize, ySize)
  }
}

package com.itszuvalex.femtocraft.logistics.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.logistics.container.ContainerItemRepository
import com.itszuvalex.femtocraft.logistics.gui.GuiItemRepository._
import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository
import com.itszuvalex.itszulib.gui.GuiBase
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
object GuiItemRepository {
  val TEXTURE_LOC    = Resources.TexGui("GuiItemRepository.png")
  val TEXTURE_HEIGHT = 211
}

class GuiItemRepository(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileItemRepository)
  extends GuiBase(new ContainerItemRepository(player, inv, tile)) {
  ySize = TEXTURE_HEIGHT

  override def drawGuiContainerBackgroundLayer(p_146976_1_ : Float, p_146976_2_ : Int, p_146976_3_ : Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    Minecraft.getMinecraft.getTextureManager.bindTexture(TEXTURE_LOC)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 0, xSize, ySize)
  }
}

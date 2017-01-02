package com.itszuvalex.femtocraft.power.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.power.container.ContainerCrystalMount
import com.itszuvalex.femtocraft.power.gui.GuiCrystalMount._
import com.itszuvalex.femtocraft.power.tile.TileCrystalMount
import com.itszuvalex.itszulib.container.ContainerBase
import com.itszuvalex.itszulib.container.sync.SyncItemStorageItemStack
import com.itszuvalex.itszulib.gui.{GuiBase, GuiIItemStorageSlot}
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
object GuiCrystalMount {
  val TEXTURE_LOC = Resources.TexGui("guicrystalmount.png")
}

class GuiCrystalMount(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileCrystalMount)
  extends GuiBase(new ContainerCrystalMount(player, inv, tile, false)) {

  {
    val gui = new GuiIItemStorageSlot(79, 33, tile.storage, 0)
    gui.sync = new SyncItemStorageItemStack(tile.storage, 0)
    this.add(gui)
    inventorySlots.asInstanceOf[ContainerBase].addSync(gui.sync)
  }

  addPlayerInventorySlots(inv)

  override def drawGuiContainerBackgroundLayer(p_146976_1_ : Float, p_146976_2_ : Int, p_146976_3_ : Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    Minecraft.getMinecraft.getTextureManager.bindTexture(TEXTURE_LOC)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 0, xSize, ySize)

    super.drawGuiContainerBackgroundLayer(p_146976_1_, p_146976_2_, p_146976_3_)
  }
}

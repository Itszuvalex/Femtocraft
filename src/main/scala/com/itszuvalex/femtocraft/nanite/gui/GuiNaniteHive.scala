package com.itszuvalex.femtocraft.nanite.gui

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.nanite.container.ContainerNaniteHive
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteHiveSmall
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.container.ContainerBase
import com.itszuvalex.itszulib.container.sync.SyncItemStorageItemStack
import com.itszuvalex.itszulib.gui.{GuiBase, GuiIItemStorageSlot}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher on 9/1/2015.
  */
@SideOnly(Side.CLIENT) object GuiNaniteHive {
  val texture         = new ResourceLocation(Femtocraft.ID.toLowerCase, "textures/guis/guinanitehive_small.png")
  val WIDTH           = 226
  val HEIGHT          = 166
  val inventoryXStart = 32
  val inventoryYStart = 20
}

@SideOnly(Side.CLIENT) class GuiNaniteHive(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileNaniteHiveSmall) extends
  GuiBase(new ContainerNaniteHive(player, inv, tile, false)) {
  xSize = GuiNaniteHive.WIDTH
  ySize = GuiNaniteHive.HEIGHT

  {
    def addGuiAndSync(storage: IItemStorage, ind: Int, x: Int, y: Int): Unit = {
      var gui = new GuiIItemStorageSlot(x, y, storage, ind)
      gui.sync = new SyncItemStorageItemStack(storage, ind)
      this.add(gui)
      inventorySlots.asInstanceOf[ContainerBase].addSync(gui.sync)
    }

    val storage = Converter.IItemStorageFromIInventory(tile.indInventory)

    (0 until 3).foreach { i =>
      (0 until 9).foreach { j =>
        addGuiAndSync(storage, j + i * 9, GuiNaniteHive.inventoryXStart + j * 18, GuiNaniteHive.inventoryYStart + i * 18)
      }
    }

    addGuiAndSync(storage, 27, 204, 20)
    addGuiAndSync(storage, 28, 204, 38)
    addGuiAndSync(storage, 29, 204, 56)
  }

  addPlayerInventorySlots(inv, 32, 83)

  /**
    * Draw the foreground layer for the GuiContainer (everything in front of the items)
    */
  protected override def drawGuiContainerForegroundLayer(par1: Int, par2: Int) {
    val s = "Small Nanite Hive"
    fontRendererObj.drawString(s, xSize / 2 - fontRendererObj.getStringWidth(s) / 2, 6, Color(0, 255.toByte, 255.toByte, 255.toByte).toInt)
    fontRendererObj.drawString("container.inventory", 30, ySize - 96 + 4, Color(0, 255.toByte, 255.toByte, 255.toByte).toInt)
  }

  /**
    * Draw the background layer for the GuiContainer (everything behind the items)
    */
  override def drawGuiContainerBackgroundLayer(par1: Float, par2: Int, par3: Int) {
    super.drawGuiContainerBackgroundLayer(par1, par2, par3)
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

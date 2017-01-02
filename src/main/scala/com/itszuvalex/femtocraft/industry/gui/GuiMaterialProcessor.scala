package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.industry.container.ContainerMaterialProcessor
import com.itszuvalex.femtocraft.industry.tile.TileMaterialProcessor
import com.itszuvalex.femtocraft.util.StringUtil
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.container.ContainerBase
import com.itszuvalex.itszulib.container.sync.SyncItemStorageItemStack
import com.itszuvalex.itszulib.gui.{GuiBase, GuiIItemStorageSlot}
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.opengl.GL11

object GuiMaterialProcessor {
  val texture = Resources.TexGui("guimaterialprocessor.png")
}

@SideOnly(Side.CLIENT) class GuiMaterialProcessor(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileMaterialProcessor) extends
  GuiBase(new ContainerMaterialProcessor(player, inv, tile, false)) {

  {
    val storage = Converter.IItemStorageFromIInventory(tile.indInventory)

    def addGuiAndSync(storage: IItemStorage, ind: Int, x: Int, y: Int) = {
      var gui = new GuiIItemStorageSlot(x, y, storage, ind)
      gui.sync = new SyncItemStorageItemStack(storage, ind)
      this.add(gui)
      inventorySlots.asInstanceOf[ContainerBase].addSync(gui.sync)
    }

    addGuiAndSync(storage, 0, 34, 7)
    addGuiAndSync(storage, 1, 52, 7)
    addGuiAndSync(storage, 2, 34, 25)
    addGuiAndSync(storage, 3, 52, 25)
    addGuiAndSync(storage, 4, 151, 44)
    addGuiAndSync(storage, 5, 151, 62)
    addGuiAndSync(storage, 6, 34, 44)
    addGuiAndSync(storage, 7, 52, 44)
    addGuiAndSync(storage, 8, 34, 62)
    addGuiAndSync(storage, 9, 52, 62)
    addGuiAndSync(storage, 10, 9, 63)
    addGuiAndSync(storage, 11, 151, 7)

    // ----------------
    addPlayerInventorySlots(inv)
  }

  override def drawScreen(par1: Int, par2: Int, par3: Float) {
    super.drawScreen(par1, par2, par3)
    val text = StringUtil.formatPowerString(tile.getPowerCurrent.toLong, tile.getPowerMax.toLong)
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
  override def drawGuiContainerBackgroundLayer(par1: Float, par2: Int, par3: Int) {
    super.drawGuiContainerBackgroundLayer(par1, par2, par3)
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

package com.itszuvalex.femtocraft.logistics.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.logistics.container.ContainerNanoPack
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import com.itszuvalex.itszulib.gui.{GuiBase, GuiLabel}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import org.lwjgl.opengl.GL11

class GuiNanoPack(player: EntityPlayer, inv: InventoryPlayer, stack: IItemStack)
  extends GuiBase(new ContainerNanoPack(player, inv, stack, false)) {

  val fRender = Minecraft.getMinecraft.fontRenderer

  val storage = stack.getModule(ItszuLibModules.ITEM_STORAGE, null)
  (0 until 18).foreach { i =>
    addGuiAndSync(storage, i, 8 + (i % 9) * 18, 12 + (i / 9) * 18)
  }

  addPlayerInventorySlots(inv)

  val tileName  = "Nano Pack"
  val nameLabel = new GuiLabel(3, 3, fRender.getStringWidth(tileName), fRender.FONT_HEIGHT, () => tileName)
  add(nameLabel)

  override def GuiID: Int = GuiIDs.ItemNanoPackID

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    //    GL11.glDisable(GL11.GL_BLEND)
    GL11.glDisable(GL11.GL_LIGHTING)
    drawBackgroundLayers()
    GL11.glEnable(GL11.GL_BLEND)
    super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY)
  }

  private def drawBackgroundLayers(): Unit = {
    val k          = (width - xSize) / 2
    val l          = (height - ySize) / 2
    val blackColor = Color(255.toByte, 0, 0, 0).toInt
    Gui.drawRect(k, l, k + xSize, l + ySize, blackColor)

    var offset = 1
    //    if (tile.hasCapability(ItszuLibCapabilities.COLORABLE, null)) {
    //      val color = tile.getCapability(ItszuLibCapabilities.COLORABLE, null).toInt
    //      Gui.drawRect(k + offset, l + offset, k + xSize - offset, l + ySize - offset, color)
    //      offset += 1
    //    }

    val greyColor = Color(255.toByte, 30.toByte, 30.toByte, 30.toByte).toInt
    Gui.drawRect(k + offset, l + offset, k + xSize - offset, l + ySize - offset, greyColor)
  }
}

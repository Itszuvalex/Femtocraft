package com.itszuvalex.femtocraft.cyber.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.cyber.container.ContainerGrowthChamber
import com.itszuvalex.femtocraft.cyber.tile.TileGrowthChamber
import com.itszuvalex.itszulib.gui.{GuiBase, GuiFluidTank, GuiItemStack, GuiLabel}
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraftforge.fluids.FluidRegistry
import org.lwjgl.opengl.GL11

/**
  * Created by Alex on 18.10.2015.
  */
object GuiGrowthChamber {
  val texture = Resources.TexGui("GuiGrowthChamber.png")
}

class GuiGrowthChamber(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileGrowthChamber) extends GuiBase(new ContainerGrowthChamber(player, inv, tile)) {

  val nameLabel = new GuiLabel((panelWidth - frender.getStringWidth("Growth Chamber")) / 2, 7,
                               frender.getStringWidth("Growth Chamber"), frender.FONT_HEIGHT,
                               "Growth Chamber")
  val inputSlot = new GuiItemStack(7, 20)
  val outputSlots = {for (i <- 0 to 8) yield new GuiItemStack(79 + 18 * (i % 3), 20 + 18 * math.floor(i / 3d).toInt)}
  val waterTank = new GuiFluidTank(151, 10, this, tile, 3, FluidRegistry.WATER, true)

  def frender = Minecraft.getMinecraft.fontRenderer

  {
    val elems = List(inputSlot, waterTank) ++ outputSlots
    elems.foreach(gui => gui.setShouldRender(false))
    add(elems: _*)
  }

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glDisable(GL11.GL_LIGHTING)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiGrowthChamber.texture)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 0, xSize, ySize)

    drawTexturedModalRect(k + 27, l + 20, 180, 20, math.ceil(tile.progress * .5).toInt, 51)

    nameLabel.render(anchorX + nameLabel.anchorX, anchorY + nameLabel.anchorY, mouseX - anchorX - nameLabel.anchorX, mouseY - anchorY - nameLabel.anchorY, partialTicks)
    inputSlot.render(anchorX + inputSlot.anchorX, anchorY + inputSlot.anchorY, mouseX - anchorX - inputSlot.anchorX, mouseY - anchorY - inputSlot.anchorY, partialTicks)
    waterTank.render(anchorX + waterTank.anchorX, anchorY + waterTank.anchorY, mouseX - anchorX - waterTank.anchorX, mouseY - anchorY - waterTank.anchorY, partialTicks)
    outputSlots.foreach(gui => gui.render(anchorX + gui.anchorX, anchorY + gui.anchorY, mouseX - anchorX - gui.anchorX, mouseY - anchorY - gui.anchorY, partialTicks))
  }
}

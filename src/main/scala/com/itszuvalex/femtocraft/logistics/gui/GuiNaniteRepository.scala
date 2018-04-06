package com.itszuvalex.femtocraft.logistics.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabNaniteSideConfig}
import com.itszuvalex.femtocraft.logistics.container.ContainerNaniteRepository
import com.itszuvalex.femtocraft.logistics.tile.TileNaniteRepository
import com.itszuvalex.femtocraft.nanite.gui.GuiNaniteTank
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.{MessageDrainNanite, MessageFillNanite}
import com.itszuvalex.itszulib.gui.{GuiButton, GuiLabel}
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}

class GuiNaniteRepository(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileNaniteRepository)
  extends FemtoGuiBase(tile, new ContainerNaniteRepository(player, inv, tile, false)) {
  GuiTabNaniteSideConfig.addToGuiTabBar(tabBar, tile)

  val fRender = Minecraft.getMinecraft.fontRenderer

  addPlayerInventorySlots(inv)

  val tileName  = "Nanite Repository"
  val nameLabel = new GuiLabel(3, 3, fRender.getStringWidth(tileName), fRender.FONT_HEIGHT, () => tileName)
  add(nameLabel)

  val fillButton = new GuiButton(5, 24, 45, 16, "Fill") {
    override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
      val ret = super.onMouseClick(mouseX, mouseY, button)
      if (ret) {
        FemtoPacketHandler.INSTANCE.sendToServer(new MessageFillNanite(tile.getLoc, null))
      }
      ret
    }
  }
  add(fillButton)

  val naniteTank = new GuiNaniteTank(60, 23, tile.naniteStorageTank)
  add(naniteTank)

  val drainButton = new GuiButton(86, 24, 45, 16, "Drain") {
    override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
      val ret = super.onMouseClick(mouseX, mouseY, button)
      if (ret) {
        FemtoPacketHandler.INSTANCE.sendToServer(new MessageDrainNanite(tile.getLoc, null))
      }
      ret
    }
  }
  add(drainButton)

  override def GuiID: Int = GuiIDs.TileNaniteRepositoryGuiID

}

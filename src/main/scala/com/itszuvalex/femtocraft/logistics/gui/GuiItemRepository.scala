package com.itszuvalex.femtocraft.logistics.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabInventorySideConfig}
import com.itszuvalex.femtocraft.logistics.container.ContainerItemRepository
import com.itszuvalex.femtocraft.logistics.gui.GuiItemRepository._
import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository
import com.itszuvalex.itszulib.gui.GuiLabel
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
object GuiItemRepository {
  val TEXTURE_HEIGHT  = 211
  val inventoryStartX = 8
  val inventoryStartY = 12

}

class GuiItemRepository(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileItemRepository)
  extends FemtoGuiBase(tile, new ContainerItemRepository(player, inv, tile, false)) {
  GuiTabInventorySideConfig.addToGuiTabBar(tabBar, tile)

  val fRender = Minecraft.getMinecraft.fontRenderer

  ySize = TEXTURE_HEIGHT
  tile.storage.indices.foreach { i =>
    addGuiAndSync(tile.storage, i, inventoryStartX + (i % 9) * 18, inventoryStartY + (i / 9) * 18)
  }

  addPlayerInventorySlots(inv, ContainerItemRepository.playerInventoryStartX, ContainerItemRepository.playerInventoryStartY)

  val tileName  = "Item Repository"
  val nameLabel = new GuiLabel(3, 3, fRender.getStringWidth(tileName), fRender.FONT_HEIGHT, () => tileName)
  add(nameLabel)

  override def GuiID: Int = GuiIDs.TileItemRepositoryGuiID

}

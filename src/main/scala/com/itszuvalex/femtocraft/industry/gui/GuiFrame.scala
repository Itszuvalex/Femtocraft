package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.client.FemtoGuiBase
import com.itszuvalex.femtocraft.industry.FrameMultiblockRegistry
import com.itszuvalex.femtocraft.industry.container.ContainerFrame
import com.itszuvalex.femtocraft.industry.tile.TileFrame
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import com.itszuvalex.itszulib.gui._
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}

/**
  * Created by Christopher on 9/21/2015.
  */

class GuiFrame(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileFrame) extends FemtoGuiBase(tile, new ContainerFrame(player, inv, tile, false)) {
  fontRenderer = Minecraft.getMinecraft.fontRenderer

  (0 until 9).foreach { i =>
    addGuiAndSync(tile.multiblockStorageModule.storage, i, 7 + 18 * i, 61)
  }
  addPlayerInventorySlots(inv)

  private def state: Option[TileFrame.TileFrameState] = tile.state.get

  val nameLabel     = new GuiLabel((panelWidth - fontRenderer.getStringWidth(state.map(_.multiBlock).getOrElse(""))) / 2, 7,
                                   fontRenderer.getStringWidth(state.map(_.multiBlock).getOrElse("")), fontRenderer.FONT_HEIGHT,
                                   () => state.map(_.multiBlock).getOrElse(""))
  val requiredLabel = new GuiLabel((panelWidth - fontRenderer.getStringWidth("Required")) / 2, 9 + fontRenderer.FONT_HEIGHT,
                                   fontRenderer.getStringWidth("Required"), fontRenderer.FONT_HEIGHT,
                                   () => "Required")
  val multiblock    = FrameMultiblockRegistry.getMultiblock(state.map(_.multiBlock).getOrElse(""))
  val reqItems      = multiblock match {
    case Some(m) =>
      m.getRequiredResources.map { item =>
        new GuiItemStack(0, 0, () => false) {
          override def itemStack: IItemStack = item
        }
      }
    case None => Seq[GuiElement]()
  }
  val layout        = new GuiFlowLayout(7, 11 + fontRenderer.FONT_HEIGHT * 2, panelWidth - 14, 18, reqItems: _*)

  {
    val elements = List(nameLabel, requiredLabel, layout)

    add(elements: _*)
  }

  override def GuiID: Int = GuiIDs.TileFrameMultiblockGuiID
}


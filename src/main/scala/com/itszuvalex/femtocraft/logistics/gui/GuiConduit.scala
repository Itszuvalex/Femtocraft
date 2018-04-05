package com.itszuvalex.femtocraft.logistics.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.FemtoGuiBase
import com.itszuvalex.femtocraft.logistics.container.ContainerConduit
import com.itszuvalex.femtocraft.logistics.tile.TileConduit
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.MessageOpenGui
import com.itszuvalex.itszulib.gui.{GuiButton, GuiLabel}
import com.mojang.realmsclient.gui.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing

import scala.collection.mutable.ListBuffer

/**
  * Created by Alex on 18.08.2016.
  */
class GuiConduit(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileConduit) extends FemtoGuiBase(tile, new ContainerConduit(player, inv, tile, false)) {
  fontRenderer = Minecraft.getMinecraft.fontRenderer

  EnumFacing.VALUES.foreach { f =>
    val labelName: String = f.getName.charAt(0).toUpper.toString + ' '
    val faceButton = new GuiButton(2 + (f.getIndex / 3) * 90, 10 + (f.getIndex % 3) * 20, fontRenderer.getStringWidth(labelName) + 1, 18, labelName) {
      override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
        super.addTooltip(mouseX, mouseY, tooltip)
        if (tile.getCapability(Capabilities.TILE_CONDUIT, f).isConnected(f))
          tile.getLoc.getOffset(f).getTileEntity(false) match {
            case None =>
            case Some(null) =>
            case Some(t) =>
              tooltip += Option(t.getBlockType).map(_.getLocalizedName).getOrElse("")
          }
      }

      override def update(): Unit = {
        text =
          (if (tile.getCapability(Capabilities.TILE_CONDUIT, f).isConnected(f)) ChatFormatting.GREEN else ChatFormatting.RED) + labelName + ChatFormatting.RESET + ' '
      }

      override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
        val ret = super.onMouseClick(mouseX, mouseY, button)
        if (ret) {
          FemtoPacketHandler.INSTANCE.sendToServer(new MessageOpenGui(tile, GuiIDs.getTileConduitSideID(f)))
        }
        ret
      }
    }
    add(faceButton)

    val storage = tile.conduit.connectionStorage(f.getIndex)
    storage.indices.foreach { i =>
      addGuiAndSync(storage, i, 10 + (f.getIndex / 3) * 90 + i * 18, 10 + (f.getIndex % 3) * 20)
    }
  }

  addPlayerInventorySlots(inv)


  val nameLabel = new GuiLabel(2, 2, fontRenderer.getStringWidth("Conduit"), fontRenderer.FONT_HEIGHT, () => "Conduit")

  val elems = List(nameLabel)
  add(elems: _*)

  //  elems.foreach(e => e.setShouldRender(false))
  override def GuiID: Int = GuiIDs.TileConduitID
}

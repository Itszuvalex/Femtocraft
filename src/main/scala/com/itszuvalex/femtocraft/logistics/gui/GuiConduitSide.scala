package com.itszuvalex.femtocraft.logistics.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.FemtoGuiBase
import com.itszuvalex.femtocraft.logistics.container.ContainerConduitSide
import com.itszuvalex.femtocraft.logistics.tile.TileConduit
import com.itszuvalex.itszulib.gui.GuiLabel
import com.mojang.realmsclient.gui.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing

import scala.collection.mutable.ListBuffer

/**
  * Created by Alex on 18.08.2016.
  */
class GuiConduitSide(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileConduit, facing: EnumFacing) extends FemtoGuiBase(tile, new ContainerConduitSide(player, inv, tile, facing, false)) {
  fontRenderer = Minecraft.getMinecraft.fontRenderer

  if (facing != null) {
    val labelName: String = facing.getName.charAt(0).toUpper.toString
    val faceLabel = new GuiLabel(3 + (facing.getIndex / 3) * 90, 14 + (facing.getIndex % 3) * 20, fontRenderer.getStringWidth(labelName), fontRenderer.FONT_HEIGHT, () => {
      (if (tile.getCapability(Capabilities.TILE_CONDUIT, facing).isConnected(facing)) ChatFormatting.GREEN else ChatFormatting.RED) + labelName + ChatFormatting.RESET
    }) {
      override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
        super.addTooltip(mouseX, mouseY, tooltip)
        if (tile.getCapability(Capabilities.TILE_CONDUIT, facing).isConnected(facing))
          tile.getLoc.getOffset(facing).getTileEntity(false) match {
            case None =>
            case Some(null) =>
            case Some(t) =>
              tooltip += Option(t.getBlockType).map(_.getLocalizedName).getOrElse("")
          }
      }
    }

    add(faceLabel)

    val storage = tile.conduit.connectionStorage(facing.getIndex)
    storage.indices.foreach { i =>
      addGuiAndSync(storage, i, 10 + (facing.getIndex / 3) * 90 + i * 18, 10 + (facing.getIndex % 3) * 20)
    }

    addPlayerInventorySlots(inv)
  }


  val nameLabel = new GuiLabel(2, 2, fontRenderer.getStringWidth("Conduit"), fontRenderer.FONT_HEIGHT, () => "Conduit")

  val elems = List(nameLabel)
  add(elems: _*)

  //  elems.foreach(e => e.setShouldRender(false))
  override def GuiID: Int = GuiIDs.TileConduitID
}

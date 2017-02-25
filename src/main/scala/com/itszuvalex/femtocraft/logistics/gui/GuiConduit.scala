package com.itszuvalex.femtocraft.logistics.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.client.FemtoGuiBase
import com.itszuvalex.femtocraft.logistics.container.ContainerConduit
import com.itszuvalex.femtocraft.logistics.tile.TileConduit
import com.itszuvalex.itszulib.gui.GuiLabel
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.EnumFacing

/**
  * Created by Alex on 18.08.2016.
  */
class GuiConduit(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileConduit) extends FemtoGuiBase(tile, new ContainerConduit(player, inv, tile, false)) {
  fontRendererObj = Minecraft.getMinecraft.fontRendererObj

  EnumFacing.VALUES.foreach { f =>
    val storage = tile.conduit.connectionStorage(f.getIndex)
    storage.indices.foreach { i =>
      addGuiAndSync(storage, i, 10 + (f.getIndex / 3) * 76 + i*18, 10 + (f.getIndex % 3) * 20)
    }
  }

  addPlayerInventorySlots(inv)


  val nameLabel = new GuiLabel(2, 2, fontRendererObj.getStringWidth("Conduit"), fontRendererObj.FONT_HEIGHT, () => "Conduit")

  val elems = List(nameLabel)
  add(elems: _*)

  //  elems.foreach(e => e.setShouldRender(false))
  override def GuiID: Int = GuiIDs.TileFurnaceGuiID
}

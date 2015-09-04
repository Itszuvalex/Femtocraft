package com.itszuvalex.femtocraft.proxy

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.industry.gui.GuiArcFurnace
import com.itszuvalex.femtocraft.industry.tile.TileArcFurnace
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World

/**
 * Created by Christopher Harris (Itszuvalex) on 11/21/14.
 */
class ProxyGuiClient extends ProxyGuiCommon {
  override def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    (ID, world.getTileEntity(x, y, z)) match {
      case (GuiIDs.ArcFurnaceGuiID, te: TileArcFurnace) => new GuiArcFurnace(player, player.inventory, te)
      case (GuiIDs.ArcFurnaceGuiID, te: TileArcFurnace) => new GuiArcFurnace(player, player.inventory, te)
      case (_, _)                                       => null
    }
  }
}

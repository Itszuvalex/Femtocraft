package com.itszuvalex.femtocraft.proxy

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.industry.container.ContainerArcFurnace
import com.itszuvalex.femtocraft.industry.tile.TileArcFurnace
import com.itszuvalex.femtocraft.nanite.container.ContainerNaniteHive
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteHiveSmall
import cpw.mods.fml.common.network.IGuiHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World

/**
 * Created by Christopher Harris (Itszuvalex) on 11/21/14.
 */
class ProxyGuiCommon extends IGuiHandler {
  override def getServerGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    (ID, world.getTileEntity(x, y, z)) match {
      case (GuiIDs.ArcFurnaceGuiID, te: TileArcFurnace) => new ContainerArcFurnace(player, player.inventory, te)
      case (GuiIDs.NaniteHiveGuiID, te: TileNaniteHiveSmall) => new ContainerNaniteHive(player, player.inventory, te)
      case (_, _) => null
    }
  }

  override def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = null
}

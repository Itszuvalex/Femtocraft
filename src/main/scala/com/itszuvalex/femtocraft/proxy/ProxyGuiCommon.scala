package com.itszuvalex.femtocraft.proxy

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.core.Cyber.container.ContainerCyberBase
import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.femtocraft.core.Industry.container.{ContainerMultiblockSelection, ContainerFrame}
import com.itszuvalex.femtocraft.core.Industry.tile.TileFrame
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
      case (GuiIDs.FrameMultiblockSelectorGuiID, _)          => new ContainerMultiblockSelection
      case (GuiIDs.FrameMultiblockGuiID, te: TileFrame)      => new ContainerFrame(player, player.inventory, te)
      case (GuiIDs.CyberBaseGuiID, te: TileCyberBase)        => new ContainerCyberBase(player, player.inventory, te)
      case (GuiIDs.ArcFurnaceGuiID, te: TileArcFurnace)      => new ContainerArcFurnace(player, player.inventory, te)
      case (GuiIDs.NaniteHiveGuiID, te: TileNaniteHiveSmall) => new ContainerNaniteHive(player, player.inventory, te)
      case (_, _)                                            => null
    }
  }

  override def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = null
}

package com.itszuvalex.femtocraft.proxy

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.cyber.container.{ContainerCyberBase, ContainerGrowthChamber, ContainerMachineSelection}
import com.itszuvalex.femtocraft.cyber.tile.{TileCyberBase, TileGrowthChamber}
import com.itszuvalex.femtocraft.industry.container.{ContainerArcFurnace, ContainerFrame, ContainerMultiblockSelection}
import com.itszuvalex.femtocraft.industry.tile.{TileArcFurnace, TileFrame}
import com.itszuvalex.femtocraft.logistics.container.ContainerItemRepository
import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository
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
      case (GuiIDs.TileFrameMultiblockSelectorGuiID, _) => new ContainerMultiblockSelection
      case (GuiIDs.TileFrameMultiblockGuiID, te: TileFrame) => new ContainerFrame(player, player.inventory, te)
      case (GuiIDs.TileCyberBaseGuiID, te: TileCyberBase) => new ContainerCyberBase(player, player.inventory, te)
      case (GuiIDs.TileCyberBaseBuildGuiID, te: TileCyberBase) => new ContainerMachineSelection(te)
      case (GuiIDs.TileArcFurnaceGuiID, te: TileArcFurnace) => new ContainerArcFurnace(player, player.inventory, te)
      case (GuiIDs.TileNaniteHiveGuiID, te: TileNaniteHiveSmall) => new ContainerNaniteHive(player, player.inventory, te)
      case (GuiIDs.TileGrowthChamberGuiID, te: TileGrowthChamber) => new ContainerGrowthChamber(player, player.inventory, te)
      case (GuiIDs.TileItemRepositoryGuiID, te:TileItemRepository) => new ContainerItemRepository(player, player.inventory, te)
      case (_, _) => null
    }
  }

  override def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = null
}

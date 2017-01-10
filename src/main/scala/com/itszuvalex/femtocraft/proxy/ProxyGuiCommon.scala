package com.itszuvalex.femtocraft.proxy

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.industry.container._
import com.itszuvalex.femtocraft.industry.tile.{TileFrame, TileNanoFurnace}
import com.itszuvalex.femtocraft.logistics.container.ContainerItemRepository
import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository
import com.itszuvalex.femtocraft.nanite.container.ContainerNaniteHive
import com.itszuvalex.femtocraft.nanite.tile.{TileNaniteExtractor, TileNaniteHiveSmall}
import com.itszuvalex.femtocraft.power.container.{ContainerCrystalChargingArray, ContainerCrystalMount, ContainerCrystalStorageArray}
import com.itszuvalex.femtocraft.power.tile.{TileCrystalChargingArray, TileCrystalMount, TileCrystalStorageArray}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler

/**
  * Created by Christopher Harris (Itszuvalex) on 11/21/14.
  */
class ProxyGuiCommon extends IGuiHandler {
  override def getServerGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    (ID, world.getTileEntity(new BlockPos(x, y, z))) match {
      case (GuiIDs.TileFrameMultiblockSelectorGuiID, _) => new ContainerMultiblockSelection
      case (GuiIDs.TileFrameMultiblockGuiID, te: TileFrame) => new ContainerFrame(player, player.inventory, te)
      case (GuiIDs.TileFrameConstructingGuiID, te: TileFrame) => new ContainerFrameConstructing(player, player.inventory, te)
      case (GuiIDs.TileNaniteHiveGuiID, te: TileNaniteHiveSmall) => new ContainerNaniteHive(player, player.inventory, te, true)
      case (GuiIDs.TileItemRepositoryGuiID, te: TileItemRepository) => new ContainerItemRepository(player, player.inventory, te)
      case (GuiIDs.TileCrystalMountGuiID, te: TileCrystalMount) => new ContainerCrystalMount(player, player.inventory, te)
      case (GuiIDs.TileFurnaceGuiID, te: TileNanoFurnace) => new ContainerNanoFurnace(player, player.inventory, te, true)
      case (GuiIDs.TileNaniteExtractorID, te: TileNaniteExtractor) => new ContainerNaniteExtractor(player, player.inventory, te, true)
      case (GuiIDs.TileCrystalChargingArrayID, te: TileCrystalChargingArray) => new ContainerCrystalChargingArray(player, player.inventory, te, true)
      case (GuiIDs.TileCrystalStorageArrayID, te: TileCrystalStorageArray) => new ContainerCrystalStorageArray(player, player.inventory, te, true)
      case (_, _) => null
    }
  }

  override def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = null
}

package com.itszuvalex.femtocraft.proxy

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.industry.gui._
import com.itszuvalex.femtocraft.industry.tile._
import com.itszuvalex.femtocraft.logistics.gui.{GuiConduit, GuiFluidRepository, GuiItemRepository, GuiNaniteRepository}
import com.itszuvalex.femtocraft.logistics.tile.{TileConduit, TileFluidRepository, TileItemRepository, TileNaniteRepository}
import com.itszuvalex.femtocraft.nanite.gui.GuiNaniteHive
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteHiveSmall
import com.itszuvalex.femtocraft.power.gui._
import com.itszuvalex.femtocraft.power.tile.{TileCrystalChargingArray, TileCrystalHeatExchanger, TileCrystalMount, TileCrystalStorageArray}
import com.itszuvalex.itszulib.core.TileEntityBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 11/21/14.
  */
class ProxyGuiClient extends ProxyGuiCommon {
  override def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    (ID, world.getTileEntity(new BlockPos(x, y, z))) match {
      case (GuiIDs.TileFrameMultiblockSelectorGuiID, _) => new GuiMultiblockSelection(player, player.getHeldItemMainhand)
      case (GuiIDs.TileFrameMultiblockGuiID, te: TileFrame) => new GuiFrame(player, player.inventory, te)
      case (GuiIDs.TileFrameConstructingGuiID, te: TileFrame) => new GuiFrameConstructing(player, player.inventory, te)
      case (GuiIDs.TileNaniteHiveGuiID, te: TileNaniteHiveSmall) => new GuiNaniteHive(player, player.inventory, te)
      case (GuiIDs.TileItemRepositoryGuiID, te: TileItemRepository) => new GuiItemRepository(player, player.inventory, te)
      case (GuiIDs.TileNaniteRepositoryGuiID, te: TileNaniteRepository) => new GuiNaniteRepository(player, player.inventory, te)
      case (GuiIDs.TileFluidRepositoryGuiID, te: TileFluidRepository) => new GuiFluidRepository(player, player.inventory, te)
      case (GuiIDs.TileCrystalMountGuiID, te: TileCrystalMount) => new GuiCrystalMount(player, player.inventory, te)
      case (GuiIDs.TileFurnaceGuiID, te: TileNanoFurnace) => new GuiNanoFurnace(player, player.inventory, te)
      case (GuiIDs.TileNaniteExtractorID, te: TileNaniteExtractor) => new GuiNaniteExtractor(player, player.inventory, te)
      case (GuiIDs.TileNaniteInfuserID, te: TileNaniteInfuser) => new GuiNaniteInfuser(player, player.inventory, te)
      case (GuiIDs.TileCrystalChargingArrayID, te: TileCrystalChargingArray) => new GuiCrystalChargingArray(player, player.inventory, te)
      case (GuiIDs.TileCrystalStorageArrayID, te: TileCrystalStorageArray) => new GuiCrystalStorageArray(player, player.inventory, te)
      case (GuiIDs.TileCrystalHeatExchangerID, te: TileCrystalHeatExchanger) => new GuiCrystalHeatExchanger(player, player.inventory, te)
      case (GuiIDs.TileDemolisherGuiID, te: TileDemolisher) => new GuiDemolisher(player, player.inventory, te)
      case (GuiIDs.TileGerminationChamberID, te: TileGerminationChamber) => new GuiGerminationChamber(player, player.inventory, te)
      case (GuiIDs.TilePowerNetworkID, te: TileEntityBase) => new GuiPowerNetwork(te)
      case (GuiIDs.TileConduitID, te: TileConduit) => new GuiConduit(player, player.inventory, te)
      case (GuiIDs.TileSidedInventoryConfigID, te: TileEntity) => new GuiSidedInventoryConfig(te)
      case (GuiIDs.TileSidedNaniteConfigID, te: TileEntity) => new GuiSidedNaniteConfig(te)
      case (GuiIDs.TileSidedFluidConfigID, te: TileEntity) => new GuiSidedFluidConfig(te)
      case (_, _) => null
    }
  }
}

package com.itszuvalex.femtocraft.proxy

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.industry.container._
import com.itszuvalex.femtocraft.industry.tile._
import com.itszuvalex.femtocraft.logistics.container._
import com.itszuvalex.femtocraft.logistics.tile.{TileConduit, TileFluidRepository, TileItemRepository, TileNaniteRepository}
import com.itszuvalex.femtocraft.power.container._
import com.itszuvalex.femtocraft.power.tile.{TileCrystalChargingArray, TileCrystalHeatExchanger, TileCrystalMount, TileCrystalStorageArray}
import com.itszuvalex.itszulib.api.wrappers.{Converter, ITileEntity}
import com.itszuvalex.itszulib.gui.ItszuGuiHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 11/21/14.
  */
class ProxyGuiCommon extends ItszuGuiHandler {
  override def getServerGuiElement(ID: Int, data: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    (ID, world.getTileEntity(new BlockPos(x, y, z))) match {
      case (GuiIDs.TileFrameMultiblockSelectorGuiID, _) => new ContainerMultiblockSelection
      case (GuiIDs.ItemNanoPackID, _) => new ContainerNanoPack(player, player.inventory, Converter.IItemStackFromItemStack(player.getHeldItemMainhand), true)
      case (GuiIDs.TileFrameMultiblockGuiID, te: TileFrame) => new ContainerFrame(player, player.inventory, te, true)
      case (GuiIDs.TileFrameConstructingGuiID, te: TileFrame) => new ContainerFrameConstructing(player, player.inventory, te)
      case (GuiIDs.TileItemRepositoryGuiID, te: TileItemRepository) => new ContainerItemRepository(player, player.inventory, te, true)
      case (GuiIDs.TileNaniteRepositoryGuiID, te: TileNaniteRepository) => new ContainerNaniteRepository(player, player.inventory, te, true)
      case (GuiIDs.TileFluidRepositoryGuiID, te: TileFluidRepository) => new ContainerFluidRepository(player, player.inventory, te, true)
      case (GuiIDs.TileCrystalMountGuiID, te: TileCrystalMount) => new ContainerCrystalMount(player, player.inventory, te)
      case (GuiIDs.TileFurnaceGuiID, te: TileNanoFurnace) => new ContainerNanoFurnace(player, player.inventory, te, true)
      case (GuiIDs.TileNaniteExtractorID, te: TileNaniteExtractor) => new ContainerNaniteExtractor(player, player.inventory, te, true)
      case (GuiIDs.TileNaniteInfuserID, te: TileNaniteInfuser) => new ContainerNaniteInfuser(player, player.inventory, te, true)
      case (GuiIDs.TileCrystalChargingArrayID, te: TileCrystalChargingArray) => new ContainerCrystalChargingArray(player, player.inventory, te, true)
      case (GuiIDs.TileCrystalStorageArrayID, te: TileCrystalStorageArray) => new ContainerCrystalStorageArray(player, player.inventory, te, true)
      case (GuiIDs.TileCrystalHeatExchangerID, te: TileCrystalHeatExchanger) => new ContainerCrystalHeatExchanger(player, player.inventory, te, true)
      case (GuiIDs.TileDemolisherGuiID, te: TileDemolisher) => new ContainerDemolisher(player, player.inventory, te, true)
      case (GuiIDs.TileGerminationChamberID, te: TileGerminationChamber) => new ContainerGerminationChamber(player, player.inventory, te, true)
      case (GuiIDs.TilePowerNetworkID, te: TileEntity) => new ContainerPowerNetwork(Converter.ITileEntityFromTileEntity(te), true)
      case (GuiIDs.TileConduitID, te: TileConduit) => new ContainerConduit(player, player.inventory, te, true)
      case (GuiIDs.TileConduitSideID, te: TileConduit) => new ContainerConduitSide(player, player.inventory, te, EnumFacing.getFront(data), true)
      case (GuiIDs.TileSidedInventoryConfigID, te: ITileEntity) => new ContainerSidedInventoryConfig(te)
      case (GuiIDs.TileSidedNaniteConfigID, te: ITileEntity) => new ContainerSidedNaniteConfig(te)
      case (GuiIDs.TileSidedFluidConfigID, te: ITileEntity) => new ContainerSidedFluidConfig(te)
      case (_, _) => null
    }
  }

  override def getClientGuiElement(ID: Int, data: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = null
}

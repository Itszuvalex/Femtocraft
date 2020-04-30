package com.itszuvalex.femtocraft

import net.minecraft.util.EnumFacing

/**
  * Created by Christopher on 9/1/2015.
  */
object GuiIDs {


  val TileCrystalMountGuiID            = nextID
  val TileMaterialProcessorGuiID       = nextID
  val TileCubic2DCraftingGuiID         = nextID
  val TileCubic3DCraftingGuiID         = nextID
  val TileNaniteHiveGuiID              = nextID
  val TileFrameMultiblockSelectorGuiID = nextID
  val TileFrameMultiblockGuiID         = nextID
  val TileFrameConstructingGuiID       = nextID
  val TileItemRepositoryGuiID          = nextID
  val TileNaniteRepositoryGuiID        = nextID
  val TileFluidRepositoryGuiID         = nextID
  val TileFurnaceGuiID                 = nextID
  val TileDemolisherGuiID              = nextID
  val TileNaniteExtractorID            = nextID
  val TileNaniteInfuserID              = nextID
  val TileCrystalChargingArrayID       = nextID
  val TileCrystalStorageArrayID        = nextID
  val TileCrystalHeatExchangerID = nextID
  val TileWirelessPowerNetworkID = nextID
  val TileConduitID              = nextID

  val TileConduitSideID = nextID
  val TileSidedInventoryConfigID = nextID
  val TileSidedNaniteConfigID    = nextID
  val TileSidedFluidConfigID     = nextID
  val TileGerminationChamberID   = nextID

  //
  val TileCrystalFurnaceID = nextID
  val TileCrystalCrusherID = nextID

  val ItemNanoPackID = nextID

  private var n = 0

  def getTileConduitSideID(facing: EnumFacing): Int = (facing.getIndex << 16) + TileConduitSideID

  def isTileConduitGUI(id: Int): Boolean = (id & 0xFFFF) == TileConduitSideID

  def getTileConduitSide(id: Int): EnumFacing = EnumFacing.getFront((id & 0xFFFF0000) >> 16)

  private def nextID = {
    n += 1
    n - 1
  }
}

package com.itszuvalex.femtocraft

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
  val TileCrystalHeatExchangerID       = nextID
  val TilePowerNetworkID               = nextID
  val TileConduitID                    = nextID
  val TileConduiConfigID               = nextID
  val TileSidedInventoryConfigID       = nextID
  val TileSidedNaniteConfigID          = nextID
  val TileSidedFluidConfigID           = nextID
  private var n = 0

  private def nextID = {
    n += 1
    n - 1
  }
}

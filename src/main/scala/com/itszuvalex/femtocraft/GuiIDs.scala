package com.itszuvalex.femtocraft

/**
  * Created by Christopher on 9/1/2015.
  */
object GuiIDs {

  val TileCrystalMountGuiID            = nextID
  val TileArcFurnaceGuiID              = nextID
  val TileCentrifugeGuiID              = nextID
  val TileCrystallizerGuiID            = nextID
  val TileCubic2DCraftingGuiID         = nextID
  val TileCubic3DCraftingGuiID         = nextID
  val TileNaniteHiveGuiID              = nextID
  val TileFrameMultiblockSelectorGuiID = nextID
  val TileFrameMultiblockGuiID         = nextID
  val TileCyberBaseGuiID               = nextID
  val TileCyberBaseBuildGuiID          = nextID
  val TileGrowthChamberGuiID           = nextID
  val TileItemRepositoryGuiID          = nextID
  var TileFurnaceGuiID                 = nextID
  private var n = 0

  private def nextID = {
    n += 1
    n - 1
  }
}

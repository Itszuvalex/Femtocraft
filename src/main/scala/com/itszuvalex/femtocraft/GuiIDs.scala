package com.itszuvalex.femtocraft

/**
  * Created by Christopher on 9/1/2015.
  */
object GuiIDs {
  val CrystalMountGuiID            = nextID
  val ArcFurnaceGuiID              = nextID
  val CentrifugeGuiID              = nextID
  val CrystallizerGuiID            = nextID
  val Cubic2DCraftingGuiID         = nextID
  val Cubic3DCraftingGuiID         = nextID
  val NaniteHiveGuiID              = nextID
  val FrameMultiblockSelectorGuiID = nextID
  val FrameMultiblockGuiID         = nextID
  val CyberBaseGuiID               = nextID
  val CyberBaseBuildGuiID          = nextID
  val GrowthChamberGuiID           = nextID
  private var n = 0

  private def nextID = {
    n += 1
    n - 1
  }
}

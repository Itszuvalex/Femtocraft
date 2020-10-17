package com.itszuvalex.femtocraft.power.multiblocks

import com.itszuvalex.femtocraft.industry.RectangularSimpleFrameMultiblock
import com.itszuvalex.femtocraft.power.multiblocks.MultiblockCrystalFocusingChamber.{xSize, ySize, zSize}
import com.itszuvalex.itszulib.api.wrappers.{IBlock, IItemStack}

object MultiblockCrystalFocusingChamber {
  val xSize = 2
  val ySize = 2
  val zSize = 2

  val name = "Crystal Focusing Chamber"
}

class MultiblockCrystalFocusingChamber extends RectangularSimpleFrameMultiblock {
  override def getName: String = MultiblockCrystalFocusingChamber.name

  override def getAllowedFrameTypes: Array[String] = Array("Basic")

  override def blockType: IBlock = ???

  override def getRequiredResources: IndexedSeq[IItemStack] = Array()

  override def size: (Int, Int, Int) = (xSize, ySize, zSize)

  override def multiblockRenderID: Int = ???
}

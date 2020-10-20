package com.itszuvalex.femtocraft.power.multiblocks

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.industry.RectangularSimpleFrameMultiblock
import com.itszuvalex.femtocraft.power.multiblocks.MultiblockCrystalFocusingChamber.{xSize, ySize, zSize}
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.api.wrappers.{Converter, IBlock, IItemStack}

object MultiblockCrystalFocusingChamber {
  val xSize = 2
  val ySize = 2
  val zSize = 2

  val name = "Crystal Focusing Chamber"
}

class MultiblockCrystalFocusingChamber extends RectangularSimpleFrameMultiblock {
  override def getName: String = MultiblockCrystalFocusingChamber.name

  override def getAllowedFrameTypes: Array[String] = Array("Basic")

  override def blockType: IBlock = Converter.IBlockFromBlock(FemtoBlocks.blockCrystalFocusingChamber)

  override def getRequiredResources: IndexedSeq[IItemStack] = Array[IItemStack]()

  override def size: (Int, Int, Int) = (xSize, ySize, zSize)

  override def multiblockRenderID: Int = RenderIDs.multiblockCrystalFocusingChamberID
}

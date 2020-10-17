package com.itszuvalex.femtocraft.industry.multiblocks

import com.itszuvalex.femtocraft.industry.RectangularSimpleFrameMultiblock
import com.itszuvalex.femtocraft.industry.multiblocks.MultiblockGerminationChamber.{xSize, ySize, zSize}
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.femtocraft.{FemtoBlocks, FemtoItems}
import com.itszuvalex.itszulib.api.wrappers.{Converter, IBlock, IItemStack}
import com.itszuvalex.itszulib.implicits.ItemStackImplicits._

object MultiblockGerminationChamber {
  val xSize = 2
  val ySize = 3
  val zSize = 2

  val name = "Germination Chamber"
}

class MultiblockGerminationChamber extends RectangularSimpleFrameMultiblock {

  override def blockType: IBlock = Converter.IBlockFromBlock(FemtoBlocks.blockGerminationChamber)

  override def getName: String = MultiblockGerminationChamber.name

  override def getAllowedFrameTypes: Array[String] = Array("Basic")

  override def getRequiredResources: IndexedSeq[IItemStack] = Array[IItemStack](FemtoItems.itemRiftironIngotActivated.newIStack(10))

  override def multiblockRenderID: Int = RenderIDs.germinationChamberID

  override def size: (Int, Int, Int) = (xSize, ySize, zSize)

}



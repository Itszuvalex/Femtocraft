package com.itszuvalex.femtocraft.industry.multiblocks

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.industry.IFrameMultiblock
import com.itszuvalex.femtocraft.industry.multiblocks.MultiblockGerminationChamber.{xSize, ySize, zSize}
import com.itszuvalex.femtocraft.industry.tile.TileGerminationChamber
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.item.ItemStack

object MultiblockGerminationChamber {
  val xSize = 2
  val ySize = 3
  val zSize = 2

}

class MultiblockGerminationChamber extends IFrameMultiblock {
  override def getName: String = "Germination Chamber"

  override def getAllowedFrameTypes: Array[String] = Array("Basic")

  override def canPlaceAtLocation(loc: Loc4): Boolean = getTakenLocations(loc).forall { l =>
    l.getWorld.get.isAirBlock(l.getPos) ||
      l.getBlock(true).get.isReplaceable(l.getWorld.get, l.getPos)
  }

  override def formAtLocation(loc: Loc4): Boolean = getTakenLocations(loc).forall { l =>
    l.getWorld.get.setBlockState(l.getPos, FemtoBlocks.blockGerminationChamber.getDefaultState)
    l.getTileEntity(true) match {
      case Some(te: TileGerminationChamber) =>
        // Set base Loc4
        true
      case _ => false
    }
  }

  override def formAtLocationFromItem(loc: Loc4, item: ItemStack): Boolean = formAtLocation(loc) // TODO: NBT Item

  override def getTakenLocations(loc: Loc4): collection.Set[Loc4] = {
    for {
      x <- 0 until xSize
      y <- 0 until ySize
      z <- 0 until zSize
    } yield Loc4(loc.x + x, loc.y + y, loc.z + z, loc.dim)
  }.toSet

  override def numFrames: Int = xSize * ySize * zSize

  override def getRequiredResources: IndexedSeq[ItemStack] = Array(ItemStack.EMPTY)

  override def onMultiblockBroken(loc: Loc4): Unit = getTakenLocations(loc).foreach(l => l.getWorld.get.setBlockToAir(l.getPos))

  override def multiblockRenderID: Int = RenderIDs.germinationChamberID
}

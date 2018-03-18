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
  val ySize = 2
  val zSize = 3

}

class MultiblockGerminationChamber extends IFrameMultiblock {
  override def getName: String = "Germination Chamber"

  override def getAllowedFrameTypes: Array[String] = Array("Basic")

  override def canPlaceAtLocation(loc: Loc4): Boolean = getTakenLocations(loc).forall { loc =>
    loc.getBlock(true).get.isAir(loc.getBlockState(true).get, loc.getWorld.get, loc.getPos) ||
      loc.getBlock(true).get.isReplaceable(loc.getWorld.get, loc.getPos)
  }

  override def formAtLocation(loc: Loc4): Boolean = getTakenLocations(loc).forall { loc =>
    loc.getWorld.get.setBlockState(loc.getPos, FemtoBlocks.blockGerminationChamber.getDefaultState)
    loc.getTileEntity(true) match {
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
    } yield Loc4(x, y, z, loc.dim)
  }.toSet

  override def numFrames: Int = 2 * 2 * 3

  override def getRequiredResources: IndexedSeq[ItemStack] = Array(ItemStack.EMPTY)

  override def onMultiblockBroken(loc: Loc4): Unit = getTakenLocations(loc).foreach(loc => loc.getWorld.get.setBlockToAir(loc.getPos))

  override def multiblockRenderID: Int = RenderIDs.germinationChamberID
}

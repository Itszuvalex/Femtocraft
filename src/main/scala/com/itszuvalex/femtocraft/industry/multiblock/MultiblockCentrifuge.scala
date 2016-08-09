package com.itszuvalex.femtocraft.industry.multiblock

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.industry.IFrameMultiblock
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.Set

/**
  * Created by Christopher Harris (Itszuvalex) on 8/28/15.
  */
class MultiblockCentrifuge extends IFrameMultiblock {


  override def canPlaceAtLocation(loc: Loc4): Boolean =
    getTakenLocations(loc).forall(l => l.getWorld.get.isAirBlock(loc.getPos) || l.getWorld.get.getBlockState(l.getPos).getBlock.isReplaceable(l.getWorld.get, l.getPos))

  override def getTakenLocations(loc: Loc4): Set[Loc4] = {
                                                           for {
                                                             lx <- -1 to 1
                                                             ly <- 0 until 5
                                                             lz <- -1 to 1
                                                           } yield Loc4(loc.x + lx, loc.y + ly, loc.z + lz, loc.dim)
                                                         }.toSet

  override def formAtLocation(loc: Loc4): Boolean = getTakenLocations(loc).forall(l => l.getWorld.get.setBlockState(l.getPos, FemtoBlocks.blockCentrifuge.getDefaultState))

  override def formAtLocationFromItem(loc: Loc4, item: ItemStack): Boolean = formAtLocation(loc)

  override def getName = "Centrifuge"

  @SideOnly(Side.CLIENT)
  override def multiblockRenderID: Int = RenderIDs.multiblockCentrifugeID

  override def numFrames = 3 * 5 * 3

  override def getRequiredResources: IndexedSeq[ItemStack] = for (x <- 0 until 0) yield null

  override def getAllowedFrameTypes: Array[String] = Array("Basic", "Cyber")

  override def onMultiblockBroken(loc: Loc4): Unit = getTakenLocations(loc).foreach { l => l.getWorld.get.setBlockToAir(l.getPos) }
}

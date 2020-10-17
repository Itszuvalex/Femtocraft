package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.util.WorldUtils
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.{IBlock, IItemStack}
import com.itszuvalex.itszulib.implicits.ItemStackImplicits._


abstract class RectangularSimpleFrameMultiblock extends IFrameMultiblock {

  def yieldTakenLocations(loc: Loc4): Iterable[Loc4] = {
    val size = this.size
    WorldUtils.locationsInBoxFromLoc(loc, size._1, size._2, size._3)
  }

  def blockType: IBlock

  override def canPlaceAtLocation(loc: Loc4): Boolean = WorldUtils.locationsEmptyOrReplaceable(yieldTakenLocations(loc))

  override def formAtLocation(loc: Loc4): Boolean =
    WorldUtils.formMultiblockWithLocsAtLoc(getTakenLocations(loc), blockType, loc)

  override def formAtLocationFromItem(loc: Loc4, item: IItemStack): Boolean = formAtLocation(loc) // TODO: NBT Item

  override def getTakenLocations(loc: Loc4): collection.Set[Loc4] = yieldTakenLocations(loc).toSet

  override def numFrames: Int = {val size = this.size; size._1 * size._2 * size._3}

  override def onMultiblockBroken(loc: Loc4): Unit = yieldTakenLocations(loc).foreach(l => l.getWorld.get.setBlockToAir(l.getPos))

  override def getRenderItemStack: IItemStack = blockType.toMinecraft.newIStack()
}

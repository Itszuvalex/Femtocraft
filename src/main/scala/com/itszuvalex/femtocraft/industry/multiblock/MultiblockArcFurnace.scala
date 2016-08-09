package com.itszuvalex.femtocraft.industry.multiblock

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.industry.IFrameMultiblock
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.multiblock.IMultiBlockComponent
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.Set

/**
  * Created by Christopher Harris (Itszuvalex) on 8/28/15.
  */
class MultiblockArcFurnace extends IFrameMultiblock {
  override def canPlaceAtLocation(loc: Loc4): Boolean =
    getTakenLocations(loc).forall(l => l.getWorld.get.isAirBlock(l.getPos) || l.getWorld.get.getBlockState(l.getPos).getBlock.isReplaceable(l.getWorld.get, l.getPos))

  override def formAtLocation(loc: Loc4): Boolean = {
    val locations = getTakenLocations(loc)
    if (locations.forall(l => l.getWorld.get.setBlockState(l.getPos, FemtoBlocks.blockArcFurnace.getDefaultState))) {
      locations.flatMap(_.getTileEntity(true)).collect { case n: IMultiBlockComponent => n }.map(_.formMultiBlock(loc))
      true
    }
    else false
  }

  override def getTakenLocations(loc: Loc4): Set[Loc4] = {
                                                           for {
                                                             bx <- 0 until 2
                                                             by <- 0 until 3
                                                             bz <- 0 until 2
                                                           } yield Loc4(loc.x + bx, loc.y + by, loc.z + bz, loc.dim)
                                                         }.toSet

  override def getName = "Arc Furnace"

  override def formAtLocationFromItem(loc: Loc4, item: ItemStack): Boolean = formAtLocation(loc)


  @SideOnly(Side.CLIENT)
  override def multiblockRenderID: Int = RenderIDs.multiblockArcFurnaceID

  override def numFrames = 2 * 3 * 2

  override def getRequiredResources: IndexedSeq[ItemStack] = Array(new ItemStack(FemtoBlocks.blockCyberweave, 20),
                                                                   new ItemStack(Items.IRON_INGOT, 32),
                                                                   new ItemStack(Items.GOLD_INGOT, 4),
                                                                   new ItemStack(Items.REDSTONE, 18))

  override def getAllowedFrameTypes: Array[String] = Array("Basic", "Cyber")

  override def onMultiblockBroken(loc: Loc4): Unit = getTakenLocations(loc).foreach { l => l.getWorld.get.setBlockToAir(l.getPos) }
}

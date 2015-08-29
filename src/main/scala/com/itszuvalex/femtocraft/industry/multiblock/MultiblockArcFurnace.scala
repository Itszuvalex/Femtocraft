package com.itszuvalex.femtocraft.industry.multiblock

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.core.IFrameMultiblock
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraft.world.World

/**
 * Created by Christopher Harris (Itszuvalex) on 8/28/15.
 */
class MultiblockArcFurnace extends IFrameMultiblock {
  override def canPlaceAtLocation(world: World, x: Int, y: Int, z: Int) =
    multiblockLocations(x, y, z).forall(Function.tupled(world.isAirBlock))

  def multiblockLocations(x: Int, y: Int, z: Int) =
    for {
      bx <- 0 until 2
      by <- 0 until 3
      bz <- 0 until 2
    } yield (x + bx, y + by, z + bz)


  override def formAtLocation(world: World, x: Int, y: Int, z: Int) =
    multiblockLocations(x, y, z).forall { case (lx, ly, lz) => world.setBlock(lx, ly, lz, FemtoBlocks.blockArcFurnace) }

  override def getName = "Arc Furnace"

  @SideOnly(Side.CLIENT)
  override def multiblockRenderID: Int = ???

  override def getRequiredResources: IndexedSeq[ItemStack] = for {x <- 0 until 0} yield null

  override def getAllowedFrameTypes: Array[String] = Array("Basic", "Cyber")

  override def getNumFrames: Int = 2 * 3 * 2
}

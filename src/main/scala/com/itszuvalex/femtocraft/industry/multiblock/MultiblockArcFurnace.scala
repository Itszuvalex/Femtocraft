package com.itszuvalex.femtocraft.industry.multiblock

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.core.IFrameMultiblock
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.api.core.Loc4
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraft.world.World

import scala.collection.Set

/**
 * Created by Christopher Harris (Itszuvalex) on 8/28/15.
 */
class MultiblockArcFurnace extends IFrameMultiblock {
  override def canPlaceAtLocation(world: World, x: Int, y: Int, z: Int) =
    getTakenLocations(world, x, y, z).forall(loc => world.isAirBlock(loc.x, loc.y, loc.z) || world.getBlock(loc.x, loc.y, loc.z).isReplaceable(world, loc.x, loc.y, loc.z))

  override def getTakenLocations(world: World, x: Int, y: Int, z: Int): Set[Loc4] = {
                                                                                      for {
                                                                                        bx <- 0 until 2
                                                                                        by <- 0 until 3
                                                                                        bz <- 0 until 2
                                                                                      } yield Loc4(x + bx, y + by, z + bz, world.provider.dimensionId)
                                                                                    }.toSet


  override def formAtLocation(world: World, x: Int, y: Int, z: Int) =
    getTakenLocations(world, x, y, z).forall(loc => world.setBlock(loc.x, loc.y, loc.z, FemtoBlocks.blockArcFurnace))

  override def getName = "Arc Furnace"

  @SideOnly(Side.CLIENT)
  override def multiblockRenderID: Int = RenderIDs.multiblockArcFurnaceID

  override def getRequiredResources: IndexedSeq[ItemStack] = for {x <- 0 until 0} yield null

  override def getAllowedFrameTypes: Array[String] = Array("Basic", "Cyber")

  override def getNumFrames: Int = 2 * 3 * 2
}

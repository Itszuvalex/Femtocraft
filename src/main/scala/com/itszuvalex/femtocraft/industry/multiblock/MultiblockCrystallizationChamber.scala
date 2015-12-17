package com.itszuvalex.femtocraft.industry.multiblock

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.industry.IFrameMultiblock
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.api.core.Loc4
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraft.world.World

import scala.collection.Set

/**
  * Created by Christopher Harris (Itszuvalex) on 8/28/15.
  */
class MultiblockCrystallizationChamber extends IFrameMultiblock {
  override def canPlaceAtLocation(world: World, x: Int, y: Int, z: Int): Boolean =
    getTakenLocations(world, x, y, z).forall(loc => world.isAirBlock(loc.x, loc.y, loc.z) || world.getBlock(loc.x, loc.y, loc.z).isReplaceable(world, loc.x, loc.y, loc.z))

  override def getTakenLocations(world: World, x: Int, y: Int, z: Int): Set[Loc4] = {
                                                                                      for {
                                                                                        lx <- -2 to 2
                                                                                        ly <- 0 until 3
                                                                                        lz <- -1 to 1
                                                                                      } yield Loc4(x + lx, y + ly, z + lz, world.provider.dimensionId)
                                                                                    }.toSet

  override def formAtLocation(world: World, x: Int, y: Int, z: Int): Boolean = getTakenLocations(world, x, y, z).forall(loc => world.setBlock(x, y, z, FemtoBlocks.blockCrystallizationChamber))

  override def getName = "Crystallization Chamber"

  @SideOnly(Side.CLIENT)
  override def multiblockRenderID: Int = RenderIDs.multiblockCrystallizerID

  override def numFrames = 3 * 5 * 3

  override def getRequiredResources: IndexedSeq[ItemStack] = for (x <- 0 until 0) yield null

  override def getAllowedFrameTypes: Array[String] = Array("Basic", "Cyber")

}

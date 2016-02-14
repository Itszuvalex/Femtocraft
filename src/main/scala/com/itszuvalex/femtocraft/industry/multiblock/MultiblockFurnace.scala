package com.itszuvalex.femtocraft.industry.multiblock

import com.itszuvalex.femtocraft.industry.IFrameMultiblock
import com.itszuvalex.femtocraft.industry.item.ItemMultiblock
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.femtocraft.{FemtoBlocks, FemtoItems}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.multiblock.IMultiBlockComponent
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.world.World

import scala.collection.Set

/**
  * Created by Christopher Harris (Itszuvalex) on 8/28/15.
  */
object MultiblockFurnace {
  val name = "Furnace"
}

class MultiblockFurnace extends IFrameMultiblock {
  override def canPlaceAtLocation(world: World, x: Int, y: Int, z: Int) =
    getTakenLocations(world, x, y, z).forall(loc => world.isAirBlock(loc.x, loc.y, loc.z) || world.getBlock(loc.x, loc.y, loc.z).isReplaceable(world, loc.x, loc.y, loc.z))

  override def getTakenLocations(world: World, x: Int, y: Int, z: Int): Set[Loc4] = {
                                                                                      for {
                                                                                        bx <- 0 until 2
                                                                                        by <- 0 until 3
                                                                                        bz <- 0 until 2
                                                                                      } yield Loc4(x + bx, y + by, z + bz, world.provider.dimensionId)
                                                                                    }.toSet

  override def formAtLocationFromItem(world: World, x: Int, y: Int, z: Int, item: ItemStack): Boolean = {
    formAtLocation(world, x, y, z)
  }

  override def formAtLocation(world: World, x: Int, y: Int, z: Int) = {
    val locations = getTakenLocations(world, x, y, z)
    if (locations.forall(loc => world.setBlock(loc.x, loc.y, loc.z, FemtoBlocks.blockFurnace))) {
      locations.flatMap(_.getTileEntity(true)).collect { case n: IMultiBlockComponent => n }.map(_.formMultiBlock(world, x, y, z))
      true
    }
    else false
  }

  @SideOnly(Side.CLIENT)
  override def multiblockRenderID: Int = RenderIDs.multiblockFurnaceID

  override def numFrames = 2 * 3 * 2

  override def getRequiredResources: IndexedSeq[ItemStack] = Array(new ItemStack(Blocks.cobblestone, 24))

  override def getAllowedFrameTypes: Array[String] = Array("Basic", "Cyber")

  override def onMultiblockBroken(world: World, x: Int, y: Int, z: Int): Unit = {
    var drop = false
    val itemStack = new ItemStack(FemtoItems.itemMultiblock)
    itemStack match {
      case null =>
      case i =>
        i.getItem match {
          case item: ItemMultiblock =>
            drop = true
            item.setMultiblock(itemStack, getName)
          case _ =>
        }
    }
    getTakenLocations(world, x, y, z).foreach { loc => world.setBlockToAir(loc.x, loc.y, loc.z) }
    if(drop)
      world.spawnEntityInWorld(new EntityItem(world, x, y, z, itemStack))

  }

  override def getName = MultiblockFurnace.name
}

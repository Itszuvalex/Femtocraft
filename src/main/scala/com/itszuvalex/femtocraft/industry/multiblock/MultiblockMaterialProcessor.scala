package com.itszuvalex.femtocraft.industry.multiblock

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.industry.IFrameMultiblock
import com.itszuvalex.femtocraft.industry.tile.TileMaterialProcessor
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.femtocraft.util.ItemUtils
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.multiblock.IMultiBlockComponent
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World

import scala.collection.Set

/**
  * Created by Christopher Harris (Itszuvalex) on 8/28/15.
  */
object MultiblockMaterialProcessor {
  val name = "Material Processor"
}

class MultiblockMaterialProcessor extends IFrameMultiblock {
  override def canPlaceAtLocation(world: World, x: Int, y: Int, z: Int) =
    getTakenLocations(world, x, y, z).forall(loc => world.isAirBlock(loc.x, loc.y, loc.z) || world.getBlock(loc.x, loc.y, loc.z).isReplaceable(world, loc.x, loc.y, loc.z))

  override def formAtLocationFromItem(world: World, x: Int, y: Int, z: Int, item: ItemStack): Boolean = {
    val ret = formAtLocation(world, x, y, z)
    world.getTileEntity(x, y, z) match {
      case null =>
      case i: TileMaterialProcessor =>
        world.getTileEntity(i.info.x, i.info.y, i.info.z) match {
          case null =>
          case controller: TileMaterialProcessor =>
            controller.loadInfoFromItemNBT(item.getTagCompound)
        }
    }
    ret
  }

  override def formAtLocation(world: World, x: Int, y: Int, z: Int) = {
    val locations = getTakenLocations(world, x, y, z)
    if (locations.forall(loc => world.setBlock(loc.x, loc.y, loc.z, FemtoBlocks.blockMaterialProcessor))) {
      locations.flatMap(_.getTileEntity(true)).collect { case n: IMultiBlockComponent => n }.map(_.formMultiBlock(world, x, y, z))
      true
    }
    else false
  }

  override def getTakenLocations(world: World, x: Int, y: Int, z: Int): Set[Loc4] = {
                                                                                      for {
                                                                                        bx <- 0 until 2
                                                                                        by <- 0 until 3
                                                                                        bz <- 0 until 2
                                                                                      } yield Loc4(x + bx, y + by, z + bz, world.provider.dimensionId)
                                                                                    }.toSet

  @SideOnly(Side.CLIENT)
  override def multiblockRenderID: Int = RenderIDs.multiblockFurnaceID

  override def numFrames = 2 * 3 * 2

  override def getRequiredResources: IndexedSeq[ItemStack] = Array(new ItemStack(Blocks.cobblestone, 24))

  override def getAllowedFrameTypes: Array[String] = Array("Basic", "Cyber")

  override def onMultiblockBroken(world: World, x: Int, y: Int, z: Int): Unit = {
    val itemStack = ItemUtils.makeMultiblockItem(MultiblockMaterialProcessor.name)
    world.getTileEntity(x, y, z) match {
      case null =>
      case i: TileMaterialProcessor =>
        world.getTileEntity(i.info.x, i.info.y, i.info.z) match {
          case null =>
          case controller: TileMaterialProcessor =>
            if (!itemStack.hasTagCompound)
              itemStack.setTagCompound(new NBTTagCompound)
            controller.saveInfoToItemNBT(itemStack.getTagCompound)
        }
      case _ =>
    }
    getTakenLocations(world, x, y, z).foreach { loc => world.setBlockToAir(loc.x, loc.y, loc.z) }
    if (itemStack != null)
      world.spawnEntityInWorld(new EntityItem(world, x, y, z, itemStack))
  }

  override def getName = MultiblockMaterialProcessor.name
}

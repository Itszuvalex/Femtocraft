package com.itszuvalex.femtocraft.industry.multiblock

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.industry.IFrameMultiblock
import com.itszuvalex.femtocraft.industry.tile.TileMaterialProcessor
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.femtocraft.util.ItemUtils
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.multiblock.IMultiBlockComponent
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.Set

/**
  * Created by Christopher Harris (Itszuvalex) on 8/28/15.
  */
object MultiblockMaterialProcessor {
  val name = "Material Processor"
}

class MultiblockMaterialProcessor extends IFrameMultiblock {


  override def canPlaceAtLocation(loc: Loc4): Boolean =
    getTakenLocations(loc).forall(l => l.getWorld.get.isAirBlock(l.getPos) || l.getWorld.get.getBlockState(l.getPos).getBlock.isReplaceable(l.getWorld.get, l.getPos))

  override def formAtLocationFromItem(loc: Loc4, item: ItemStack): Boolean = {
    val ret = formAtLocation(loc)
    loc.getTileEntity() match {
      case None =>
      case Some(i: TileMaterialProcessor) =>
        i.info.cLoc.getTileEntity() match {
          case None =>
          case Some(controller: TileMaterialProcessor) =>
            controller.loadInfoFromItemNBT(item.getTagCompound)
        }
    }
    ret
  }

  override def formAtLocation(loc: Loc4): Boolean = {
    val locations = getTakenLocations(loc)
    if (locations.forall(l => l.getWorld.get.setBlockState(l.getPos, FemtoBlocks.blockMaterialProcessor.getDefaultState))) {
      locations.flatMap(_.getTileEntity(true)).collect { case n: IMultiBlockComponent => n }.map(_.formMultiBlock(loc))
      true
    }
    else false
  }

  @SideOnly(Side.CLIENT)
  override def multiblockRenderID: Int = RenderIDs.multiblockFurnaceID

  override def numFrames = 2 * 3 * 2

  override def getRequiredResources: IndexedSeq[ItemStack] = Array(new ItemStack(Blocks.COBBLESTONE, 24))

  override def getAllowedFrameTypes: Array[String] = Array("Basic", "Cyber")

  override def onMultiblockBroken(loc: Loc4): Unit = {
    val itemStack = ItemUtils.makeMultiblockItem(MultiblockMaterialProcessor.name)
    loc.getTileEntity() match {
      case Some(i: TileMaterialProcessor) =>
        i.info.cLoc.getTileEntity() match {
          case None =>
          case Some(controller: TileMaterialProcessor) =>
            if (!itemStack.hasTagCompound)
              itemStack.setTagCompound(new NBTTagCompound)
            controller.saveInfoToItemNBT(itemStack.getTagCompound)
        }
      case None =>
    }
    getTakenLocations(loc).foreach(l => l.getWorld.get.setBlockToAir(l.getPos))
    if (itemStack != null)
      loc.getWorld.get.spawnEntityInWorld(new EntityItem(loc.getWorld.get, loc.x, loc.y, loc.z, itemStack))
  }

  override def getTakenLocations(loc: Loc4): Set[Loc4] = {
    for {
      bx <- 0 until 2
      by <- 0 until 3
      bz <- 0 until 2
    } yield Loc4(loc.x + bx, loc.y + by, loc.z + bz, loc.dim)
  }.toSet

  override def getName = MultiblockMaterialProcessor.name
}

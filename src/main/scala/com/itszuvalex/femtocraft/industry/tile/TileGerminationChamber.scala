package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.FrameMultiblockRegistry
import com.itszuvalex.femtocraft.industry.multiblocks.MultiblockGerminationChamber
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.common.capabilities.Capability

class TileGerminationChamber extends TileEntityBase with MultiBlockComponent {
  override def hasDescription: Boolean = true

  override def getMod: AnyRef = Femtocraft

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability match {
    case c if capability == Capabilities.TILE_MULTIBLOCK => true
    case _ => super.hasCapability(capability, facing)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = capability match {
    case c if capability == Capabilities.TILE_MULTIBLOCK => info.asInstanceOf[T]
    case _ => super.getCapability(capability, facing)
  }

  override def getRenderBoundingBox: AxisAlignedBB = {
    if (isController) {
      new AxisAlignedBB(getPos, getPos.add(MultiblockGerminationChamber.xSize, MultiblockGerminationChamber.ySize, MultiblockGerminationChamber.zSize))
    }
    else super.getRenderBoundingBox
  }

  override def onBlockBreak(): Unit = {
    if (getWorld.isRemote) return

    if (isController) {
      FrameMultiblockRegistry.getMultiblock(MultiblockGerminationChamber.name)
        .foreach {
          _.onMultiblockBroken(getLoc)
        }
    }
    else {
      getInfo.cLoc.getWorld.foreach(_.setBlockToAir(getInfo.cLoc.getPos))
    }
  }


  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: EnumFacing): Boolean = {
    if (hasGUI) {
      info.cLoc.getTileEntity() match {
        case Some(tile: TileFrame) =>
          par5EntityPlayer.openGui(tile.getMod, tile.getGuiID, tile.getWorld, tile.getPos.getX, tile.getPos.getY, tile.getPos.getZ)
        case _ =>
      }
      true
    }
    else false
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    //    compound.setTag(ITEM_SIDED_CONFIG_NBT, sidedStorageConfig.serializeNBT())
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    //    if (compound.hasKey(ITEM_SIDED_CONFIG_NBT))
    //      sidedStorageConfig.deserializeNBT(compound.getCompoundTag(ITEM_SIDED_CONFIG_NBT))
  }
}

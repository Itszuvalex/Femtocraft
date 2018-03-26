package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.multiblocks.MultiblockGerminationChamber
import com.itszuvalex.femtocraft.industry.tile.TileGerminationChamber._
import com.itszuvalex.femtocraft.industry.{FrameMultiblockRegistry, MultiblockSidedFluidStorageConfiguration, MultiblockSidedItemStorageConfiguration}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.storage.{IFluidStorage, IItemStorage}
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.items.CapabilityItemHandler

object TileGerminationChamber {
  val TICKS_FOR_AUTOIO = 20

  val TANK_SIZE = 10000

  val INPUT_INV_KEY  = "Input"
  val OUTPUT_INV_KEY = "Output"
  val NONE_INV_KEY   = "None"

  val TANK_KEY      = "Tank"
  val NONE_TANK_KEY = "None"

  val TASK_NBT               = "Task"
  val ITEM_SIDED_CONFIG_NBT  = "ItemConfig"
  val FLUID_SIDED_CONFIG_NBT = "FluidConfig"
  val TICKS_NBT              = "Ticks"
}

class TileGerminationChamber extends TileEntityBase with MultiBlockComponent {
  private val tank        : IFluidStorage = IFluidStorage.Empty
  private val storage     : IItemStorage  = IItemStorage.Empty
  private val inputStorage: IItemStorage  = IItemStorage.Empty //new ItemStorageSlice(storage, Array(0))
  private val outputStorage: IItemStorage = IItemStorage.Empty //new ItemStorageSlice(storage, Array(1))
  private val sidedStorageConfig = new MultiblockSidedItemStorageConfiguration(
      getLoc _, info, NONE_INV_KEY, _ => INPUT_INV_KEY,
      Map(NONE_INV_KEY -> IItemStorage.Empty,
        INPUT_INV_KEY -> inputStorage,
        OUTPUT_INV_KEY -> outputStorage),
      () => EnumFacing.NORTH) {

    }

  private val sidedFluidConfig = new MultiblockSidedFluidStorageConfiguration(
    getLoc _, info, NONE_TANK_KEY, _ => TANK_KEY,
    Map(NONE_TANK_KEY -> IFluidStorage.Empty,
      TANK_KEY -> tank),
    () => EnumFacing.NORTH
  )

  override def hasDescription: Boolean = true

  override def getMod: AnyRef = Femtocraft

  override def getGuiID: Int = GuiIDs.TileGerminationChamberID

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = (capability, facing) match {
    case _ if capability == com.itszuvalex.itszulib.api.Capabilities.TILE_MULTIBLOCK => true
    case _ if capability == Capabilities.ITEM_STORAGE_CONFIGURABLE => true
    case _ if capability == Capabilities.FLUID_STORAGE_CONFIGURABLE => true
    case _ if capability == com.itszuvalex.itszulib.api.Capabilities.ITEM_STORAGE => true
    case _ if capability == com.itszuvalex.itszulib.api.Capabilities.FLUID_STORAGE => true
    case _ if capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => true
    case _ if capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => true
    case _ => super.hasCapability(capability, facing)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = (capability, facing) match {
    case _ if capability == com.itszuvalex.itszulib.api.Capabilities.TILE_MULTIBLOCK => info.asInstanceOf[T]
    case _ if capability == Capabilities.ITEM_STORAGE_CONFIGURABLE => sidedStorageConfig.asInstanceOf[T]
    case _ if capability == Capabilities.FLUID_STORAGE_CONFIGURABLE => sidedFluidConfig.asInstanceOf[T]
    case (cap, null) if cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => Converter.IItemHandlerModifiableFromIItemStorage(storage).asInstanceOf[T]
    case (cap, null) if cap == com.itszuvalex.itszulib.api.Capabilities.ITEM_STORAGE => storage.asInstanceOf[T]
    case (cap, null) if cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => tank.asInstanceOf[T]
    case (cap, null) if cap == com.itszuvalex.itszulib.api.Capabilities.FLUID_STORAGE => tank.asInstanceOf[T]
    case (_, null) => super.getCapability(capability, facing)
    case (cap, _) if cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => Converter.IItemHandlerModifiableFromIItemStorage(sidedStorageConfig.getStorageForGlobalFacing(facing)).asInstanceOf[T]
    case (cap, _) if cap == com.itszuvalex.itszulib.api.Capabilities.ITEM_STORAGE => sidedStorageConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case (cap, _) if cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => sidedFluidConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case (cap, _) if cap == com.itszuvalex.itszulib.api.Capabilities.FLUID_STORAGE => sidedFluidConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
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
        case Some(tile: TileGerminationChamber) =>
          par5EntityPlayer.openGui(tile.getMod, tile.getGuiID, tile.getWorld, tile.getPos.getX, tile.getPos.getY, tile.getPos.getZ)
        case _ =>
      }
      true
    }
    else false
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    compound.setTag(ITEM_SIDED_CONFIG_NBT, sidedStorageConfig.serializeNBT())
    compound.setTag(FLUID_SIDED_CONFIG_NBT, sidedFluidConfig.serializeNBT())
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    if (compound.hasKey(ITEM_SIDED_CONFIG_NBT))
      sidedStorageConfig.deserializeNBT(compound.getCompoundTag(ITEM_SIDED_CONFIG_NBT))
    if (compound.hasKey(FLUID_SIDED_CONFIG_NBT))
      sidedFluidConfig.deserializeNBT(compound.getCompoundTag(FLUID_SIDED_CONFIG_NBT))
  }

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    if (par1nbtTagCompound.hasKey(ITEM_SIDED_CONFIG_NBT))
      sidedStorageConfig.deserializeNBT(par1nbtTagCompound.getCompoundTag(ITEM_SIDED_CONFIG_NBT))
    if (par1nbtTagCompound.hasKey(FLUID_SIDED_CONFIG_NBT))
      sidedFluidConfig.deserializeNBT(par1nbtTagCompound.getCompoundTag(FLUID_SIDED_CONFIG_NBT))
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(par1nbtTagCompound)
    par1nbtTagCompound.setTag(ITEM_SIDED_CONFIG_NBT, sidedStorageConfig.serializeNBT())
    par1nbtTagCompound.setTag(FLUID_SIDED_CONFIG_NBT, sidedFluidConfig.serializeNBT())
    par1nbtTagCompound
  }
}

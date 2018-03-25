package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.api.{Capabilities, WrapperFluidStorageHandler}
import com.itszuvalex.femtocraft.logistics.tile.TileFluidRepository._
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.storage.FluidStorageArray
import com.itszuvalex.itszulib.api.wrappers.WrapperForgeFluidTank
import com.itszuvalex.itszulib.core.traits.tile.BlockFacing
import com.itszuvalex.itszulib.core.{SidedFluidStorageConfiguration, TileEntityBase}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.FluidTank
import net.minecraftforge.fluids.capability.CapabilityFluidHandler


object TileFluidRepository {
  val TANK_SIZE              = 5000
  val TICKS_FOR_AUTOIO       = 20
  val AMT_FOR_AUTOIO         = 250
  val TICKS_NBT              = "Ticks"
  val FLUID_SIDED_CONFIG_NBT = "FluidConfig"
  val TANK_KEY               = "Tank"
  val NONE_KEY               = "None"
}

class TileFluidRepository extends TileEntityBase {
  private val tank             = new FluidTank(TANK_SIZE)
  private val stack            = new WrapperForgeFluidTank(tank)
  private val storage          = new FluidStorageArray(Array(stack))
  private val sidedFluidConfig = new SidedFluidStorageConfiguration(_ => TANK_KEY,
    Map(NONE_KEY -> null,
      TANK_KEY -> storage),
    () => world.getBlockState(pos).getValue(BlockFacing.FACING))
  var ticks = 0

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T =
    (capability, facing) match {
      case (cap, _) if cap == Capabilities.FLUID_STORAGE_CONFIGURABLE => sidedFluidConfig.asInstanceOf[T]
      case (cap, null) if cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => tank.asInstanceOf[T]
      case (_, null) => null.asInstanceOf[T]
      case (cap, face) if cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => new WrapperFluidStorageHandler(sidedFluidConfig.getStorageForGlobalFacing(face)).asInstanceOf[T]
      case (cap, face) if cap == Capabilities.FLUID_STORAGE => sidedFluidConfig.getStorageForGlobalFacing(face).asInstanceOf[T]
      case _ => super.getCapability(capability, facing)
    }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean =
    (capability, facing) match {
      case (cap, _) if cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => true
      case (cap, _) if cap == Capabilities.FLUID_STORAGE_CONFIGURABLE => true
      case (cap, _) if cap == Capabilities.FLUID_STORAGE => true
      case _ => super.hasCapability(capability, facing)
    }

  override def serverUpdate(): Unit = {
    super.serverUpdate()
//    ticks = TileEntityUtils.incrementTicks(ticks, TICKS_FOR_AUTIO)
//    TileEntityUtils.checkDoFluidInputIO(this, sidedFluidConfig, ticks, VOL_PER_AUTOIO)
//    TileEntityUtils.checkDoFluidOutputIO(this, sidedFluidConfig, ticks, VOL_PER_AUTOIO)
  }

  override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(nbt)
    nbt.setInteger(TICKS_NBT, ticks)
    nbt.setTag(FLUID_SIDED_CONFIG_NBT, sidedFluidConfig.serializeNBT())
    nbt
  }

  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)
    ticks = nbt.getInteger(TICKS_NBT)
    if (nbt.hasKey(FLUID_SIDED_CONFIG_NBT))
      sidedFluidConfig.deserializeNBT(nbt.getCompoundTag(FLUID_SIDED_CONFIG_NBT))
  }

  override def getMod = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileFluidRepositoryGuiID

  override def hasDescription: Boolean = false

}

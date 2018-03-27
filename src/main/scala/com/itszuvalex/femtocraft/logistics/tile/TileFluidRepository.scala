package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.logistics.tile.TileFluidRepository._
import com.itszuvalex.femtocraft.util.TileEntityUtils
import com.itszuvalex.femtocraft.util.data.{DataInt, DataSerializable, TileDataSpec}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.storage.FluidStorage
import com.itszuvalex.itszulib.core.traits.tile.BlockFacing
import com.itszuvalex.itszulib.core.{SidedFluidStorageConfiguration, TileEntityBase}
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.fluids.{Fluid, FluidRegistry, FluidStack, IFluidBlock}


object TileFluidRepository {
  val TANK_SIZE              = 5000
  val TICKS_FOR_AUTOIO       = 20
  val AMT_FOR_AUTOIO         = 250
  val TICKS_NBT              = "Ticks"
  val FLUID_SIDED_CONFIG_NBT = "FluidConfig"
  val TANK_KEY               = "Tank"
  val NONE_KEY               = "None"
}

class TileFluidRepository extends TileEntityBase with TileDataSpec {
  private val storage          = new FluidStorage(TANK_SIZE)
  private val sidedFluidConfig = new SidedFluidStorageConfiguration(_ => TANK_KEY,
    Map(NONE_KEY -> null,
      TANK_KEY -> storage),
    () => world.getBlockState(pos).getValue(BlockFacing.FACING))
  var ticks = 0
  private var fluidLast: Fluid = null

  descriptionDataSpec ++= Array(
    new DataSerializable[NBTTagCompound](FLUID_SIDED_CONFIG_NBT, sidedFluidConfig),
    new DataSerializable[NBTTagCompound](TANK_KEY, storage)
  )
  saveDataSpec ++= Array(
    new DataSerializable[NBTTagCompound](TANK_KEY, storage),
    new DataSerializable[NBTTagCompound](FLUID_SIDED_CONFIG_NBT, sidedFluidConfig),
    new DataInt(TICKS_NBT, ticks _, ticks_=)
  )

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T =
    (capability, facing) match {
      case (cap, _) if cap == Capabilities.FLUID_STORAGE_CONFIGURABLE => sidedFluidConfig.asInstanceOf[T]
      case (cap, null) if cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => storage.asInstanceOf[T]
      case (cap, null) if cap == com.itszuvalex.itszulib.api.Capabilities.FLUID_STORAGE => storage.asInstanceOf[T]
      case (_, null) => null.asInstanceOf[T]
      case (cap, face) if cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => sidedFluidConfig.getStorageForGlobalFacing(face).asInstanceOf[T]
      case (cap, face) if cap == com.itszuvalex.itszulib.api.Capabilities.FLUID_STORAGE => sidedFluidConfig.getStorageForGlobalFacing(face).asInstanceOf[T]
      case _ => super.getCapability(capability, facing)
    }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean =
    (capability, facing) match {
      case (cap, _) if cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => true
      case (cap, _) if cap == Capabilities.FLUID_STORAGE_CONFIGURABLE => true
      case (cap, _) if cap == com.itszuvalex.itszulib.api.Capabilities.FLUID_STORAGE => true
      case _ => super.hasCapability(capability, facing)
    }

  override def serverUpdate(): Unit = {
    super.serverUpdate()

    getLoc.getOffset(EnumFacing.DOWN).getBlock(false) match {
      case None =>
      case Some(b) if b == Blocks.WATER => storage.fill(new FluidStack(FluidRegistry.WATER, 25), true)
      case Some(b) if b.isInstanceOf[IFluidBlock] && b.asInstanceOf[IFluidBlock].getFluid == FluidRegistry.WATER => storage.fill(new FluidStack(FluidRegistry.WATER, 25), true)
      case _ =>
    }

    ticks = TileEntityUtils.incrementTicks(ticks, TICKS_FOR_AUTOIO)
    TileEntityUtils.checkDoFluidInputIO(this, sidedFluidConfig, ticks, AMT_FOR_AUTOIO)
    TileEntityUtils.checkDoFluidOutputIO(this, sidedFluidConfig, ticks, AMT_FOR_AUTOIO)

    val currentFluid = Option(storage.getStorageProperties(0).getContents).map(_.getFluid).orNull
    if (currentFluid != fluidLast)
      setUpdate()
    fluidLast = currentFluid
  }


  override def getMod = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileFluidRepositoryGuiID

  override def hasDescription: Boolean = true

}

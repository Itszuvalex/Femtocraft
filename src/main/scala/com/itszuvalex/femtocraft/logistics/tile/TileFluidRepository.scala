package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.logistics.tile.TileFluidRepository._
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.{IModule, Loc4, Module}
import com.itszuvalex.itszulib.api.storage.{FluidStorageArray, IFluidStorage}
import com.itszuvalex.itszulib.api.wrappers.{Converter, ITileEntity}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviorHorizontalFacing
import com.itszuvalex.itszulib.core.modules._
import com.itszuvalex.itszulib.core.{SidedFluidStorageConfiguration, TileEntityCoreTickable, TileEntityInternalModuleTickable}
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.fluids.{Fluid, FluidRegistry, FluidStack, IFluidBlock}

object TileFluidRepository {
  val MODULE: IModule[FluidRepositoryModule] = Module.registerModule("FluidRepositoryModule", null)

  val TANK_SIZE = 5000
  val TANK_KEY  = "Tank"
  val NONE_KEY  = "None"

  class FluidRepositoryModule(val tile: TileFluidRepository, val storage: IFluidStorage) extends TileEntityInternalModuleTickable[FluidRepositoryModule] {
    var fluidLast: Option[Fluid] = None

    override def module: IModule[FluidRepositoryModule] = TileFluidRepository.MODULE

    override def serverUpdate(tile: ITileEntity): Unit = {
      Loc4(tile).getOffset(EnumFacing.DOWN).getBlock(false) match {
        case None =>
        case Some(b) if b == Blocks.WATER => storage.fill(Converter.IFluidStackFromFluidStack(new FluidStack(FluidRegistry.WATER, 25)), true)
        case Some(b) if b.isInstanceOf[IFluidBlock] && b.asInstanceOf[IFluidBlock].getFluid == FluidRegistry.WATER => storage.fill(Converter.IFluidStackFromFluidStack(new FluidStack(FluidRegistry.WATER, 25)), true)
        case _ =>
      }

      val currentFluid = if (storage.head.isEmpty) None else Option(storage.head.fluid)
      if (currentFluid != fluidLast) {
        tile.setUpdate()
      }
      fluidLast = currentFluid
    }

    override def hasDescriptionNBT: Boolean = true

    override def writeDescriptionNBT(tag: NBTTagCompound): Unit = {
      tag.setTag(TANK_KEY, storage.serializeNBT())
    }

    override def readDescriptionNBT(tag: NBTTagCompound): Unit = {
      storage.deserializeNBT(tag.getCompoundTag(TANK_KEY))
      tile.setRenderUpdate()
    }
  }

}

class TileFluidRepository extends TileEntityCoreTickable {
  val storage: IFluidStorage = new FluidStorageArray(1, TANK_SIZE)
  val sidedFluidConfig       = new SidedFluidStorageConfiguration(_ => TANK_KEY,
                                                                  Map(NONE_KEY -> IFluidStorage.Empty,
                                                                      TANK_KEY -> storage),
                                                                  () => world.getBlockState(pos).getValue(BlockBehaviorHorizontalFacing.FACING))
  val internal               = new FluidRepositoryModule(this, storage)

  addTileEntityModule(new ModuleIFluidStorage(storage))
  addTileEntityModule(new ModuleIFluidHandlerConverter)
  addTileEntityModule(new ModuleIFluidSidedConfiguration(sidedFluidConfig))
  addTileEntityModule(new ModuleGui(Femtocraft, GuiIDs.TileFluidRepositoryGuiID _))
  addTileEntityModuleTickable(new ModuleIFluidAutoIO(sidedFluidConfig))
  addTileEntityModuleTickable(internal)
}

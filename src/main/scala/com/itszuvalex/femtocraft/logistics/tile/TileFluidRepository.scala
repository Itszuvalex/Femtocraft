package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.logistics.tile.TileFluidRepository._
import com.itszuvalex.femtocraft.temp.{ModuleIFluidAutoIO, ModuleIFluidSidedConfiguration, TileEntityInternalModuleTickable}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.{IModule, Loc4, Module}
import com.itszuvalex.itszulib.api.storage.{FluidStorage, IFluidStorage}
import com.itszuvalex.itszulib.api.wrappers.{Converter, ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviorHorizontalFacing
import com.itszuvalex.itszulib.core.modules.{ModuleIFluidHandlerConverter, ModuleIFluidStorage}
import com.itszuvalex.itszulib.core.{SidedFluidStorageConfiguration, TileEntityCoreTickable}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}
import net.minecraftforge.fluids.{Fluid, FluidRegistry, FluidStack, IFluidBlock}

object TileFluidRepository {
  val MODULE: IModule[FluidRepositoryModule] = Module.registerModule("FluidRepositoryModule", null)

  val TANK_SIZE = 5000
  val TANK_KEY  = "Tank"
  val NONE_KEY  = "None"

  class FluidRepositoryModule(val storage: IFluidStorage) extends TileEntityInternalModuleTickable[FluidRepositoryModule] {
    var fluidLast: Option[Fluid] = None

    override def module: IModule[FluidRepositoryModule] = TileFluidRepository.MODULE

    override def serverUpdate(tile: ITileEntity): Unit = {
      new Loc4(tile).getOffset(EnumFacing.DOWN).getBlock(false) match {
        case None =>
        case Some(b) if b == Blocks.WATER => storage.fill(Converter.IFluidStackFromFluidStack(new FluidStack(FluidRegistry.WATER, 25)), true)
        case Some(b) if b.isInstanceOf[IFluidBlock] && b.asInstanceOf[IFluidBlock].getFluid == FluidRegistry.WATER => storage.fill(Converter.IFluidStackFromFluidStack(new FluidStack(FluidRegistry.WATER, 25)), true)
        case _ =>
      }

      val currentFluid = if (storage.head.isEmpty) None else Option(storage.head.fluid)
      if (currentFluid != fluidLast) {
        // TODO Change to setUpdate
        tile.getIWorld.toMinecraft.notifyBlockUpdate(tile.getPos, tile.getIWorld.getBlockState(tile.getPos), tile.getIWorld.getBlockState(tile.getPos), 3)
      }
      fluidLast = currentFluid
    }

    override def hasDescriptionNBT: Boolean = true

    override def writeDescriptionNBT(tag: NBTTagCompound): Unit = {
      tag.setTag(TANK_KEY, storage.serializeNBT())
    }

    override def readDescriptionNBT(tag: NBTTagCompound): Unit = {
      storage.deserializeNBT(tag.getCompoundTag(TANK_KEY))
    }
  }

}

class TileFluidRepository extends TileEntityCoreTickable {
  val storage: IFluidStorage = new FluidStorage(TANK_SIZE)
  val sidedFluidConfig       = new SidedFluidStorageConfiguration(_ => TANK_KEY,
                                                                  Map(NONE_KEY -> IFluidStorage.Empty,
                                                                      TANK_KEY -> storage),
                                                                  () => world.getBlockState(pos).getValue(BlockBehaviorHorizontalFacing.FACING))
  val internal               = new FluidRepositoryModule(storage)

  addTileEntityModule(new ModuleIFluidStorage(storage))
  addTileEntityModule(new ModuleIFluidHandlerConverter)
  addTileEntityModule(new ModuleIFluidSidedConfiguration(sidedFluidConfig))
  addTileEntityModuleTickable(new ModuleIFluidAutoIO(sidedFluidConfig))
  addTileEntityModuleTickable(internal)

  override def getMod = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileFluidRepositoryGuiID

  override def hasDescription: Boolean = true

  // TODO
  override def onBlockActivated(world: IWorld, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = false
}

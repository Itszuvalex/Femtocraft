package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.industry.LiquifierRecipeRegistry
import com.itszuvalex.femtocraft.industry.tile.TileCrystalLiquifier._
import com.itszuvalex.femtocraft.power.{ModuleColorableFromICrystal, ModuleIBatteryFromItemStack, ModuleWiredPowerLeafNode}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.{IModule, Module}
import com.itszuvalex.itszulib.api.storage._
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviorHorizontalFacing
import com.itszuvalex.itszulib.core.modules._
import com.itszuvalex.itszulib.core.{SidedFluidStorageConfiguration, SidedItemStorageConfiguration, TileEntityCoreTickable}
import net.minecraft.util.EnumFacing

object TileCrystalLiquifier {
  val MODULE: IModule[CrystalLiquifierModule] = Module.registerModule[CrystalLiquifierModule]("CrystalLiquifierModule", null)

  val INPUT_INV_KEY  = "Input"
  val OUTPUT_INV_KEY = "Output"
  val NONE_INV_KEY   = "None"
  val TANK_SIZE      = 8000
}

class TileCrystalLiquifier extends TileEntityCoreTickable {
  val storage       : IItemStorage            = new ItemStorageArray(2) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = i match {
      case 0 =>
        LiquifierRecipeRegistry.findMatchingRecipe(stack).isDefined
      case 1 =>
        stack.isEmpty || stack.hasModule(ManagerModules.POWER_STORAGE, null)
      case _ => false
    }
  }
  val inputStorage  : IItemStorage            = new ItemStorageSlice(storage, Array(0))
  val outputStorage : IFluidStorageModifiable = new FluidStorageArray(1, TANK_SIZE)
  val batteryStorage: IItemStorage            = new ItemStorageSlice(storage, Array(1))
  val battery       : IBattery                = new DynamicIBattery(() => batteryStorage.head.moduleOption(ManagerModules.POWER_STORAGE, null).getOrElse(IBattery.Empty))
  val sidedItemStorageConfig                  = new SidedItemStorageConfiguration({
    case EnumFacing.UP | EnumFacing.SOUTH => INPUT_INV_KEY
    case _ => NONE_INV_KEY
  },
   Map(NONE_INV_KEY -> IItemStorage.Empty,
       INPUT_INV_KEY -> inputStorage
       ),
   () => world.getBlockState(pos).getValue(BlockBehaviorHorizontalFacing.FACING))
  val sidedFluidStorageConfig                 = new SidedFluidStorageConfiguration({
    case EnumFacing.DOWN | EnumFacing.EAST | EnumFacing.WEST | EnumFacing.NORTH => OUTPUT_INV_KEY
    case _ => NONE_INV_KEY
  },
   Map(NONE_INV_KEY -> IFluidStorage.Empty,
       OUTPUT_INV_KEY -> outputStorage),
   () => world.getBlockState(pos).getValue((BlockBehaviorHorizontalFacing.FACING)))

  val internal = new CrystalLiquifierModule(inputStorage, outputStorage, batteryStorage, battery)

  addTileEntityModule(new ModuleIItemStorage(storage))
  addTileEntityModule(new ModuleIItemHandlerConverter)
  addTileEntityModule(new ModuleIItemSidedConfiguration(sidedItemStorageConfig))
  addTileEntityModule(new ModuleDropInventory(storage))
  addTileEntityModule(new ModuleIFluidStorage(outputStorage))
  addTileEntityModule(new ModuleIFluidHandlerConverter)
  addTileEntityModule(new ModuleIFluidSidedConfiguration(sidedFluidStorageConfig))
  addTileEntityModule(new ModuleIBatteryFromItemStack(batteryStorage.head _))
  addTileEntityModule(new ModuleGui(Femtocraft, GuiIDs.TileCrystalLiquifierID _))
  addTileEntityModule(new ModuleWiredPowerLeafNode(this, battery _, PowerStorageNodeType.CONSUMER, () => 50d))
  addTileEntityModuleTickable(new ModuleColorableFromICrystal(this, () => batteryStorage.head.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null)))
  addTileEntityModuleTickable(new ModuleIItemAutoIO(sidedItemStorageConfig))
  addTileEntityModuleTickable(new ModuleIFluidAutoIO(sidedFluidStorageConfig))
  addTileEntityModuleTickable(internal)
}

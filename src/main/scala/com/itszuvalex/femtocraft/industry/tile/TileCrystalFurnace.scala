package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.industry.tile.TileCrystalFurnace._
import com.itszuvalex.femtocraft.power._
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.{IModule, Module}
import com.itszuvalex.itszulib.api.storage._
import com.itszuvalex.itszulib.api.wrappers.{Converter, IItemStack}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviorHorizontalFacing
import com.itszuvalex.itszulib.core.modules._
import com.itszuvalex.itszulib.core.{SidedItemStorageConfiguration, TileEntityCoreTickable}
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.util.EnumFacing

object TileCrystalFurnace {
  val MODULE: IModule[CrystalFurnaceModule] = Module.registerModule[CrystalFurnaceModule]("CrystalFurnaceModule", null)

  val INPUT_INV_KEY  = "Input"
  val OUTPUT_INV_KEY = "Output"
  val NONE_INV_KEY   = "None"
}

class TileCrystalFurnace extends TileEntityCoreTickable {
  val storage       : IItemStorage = new ItemStorageArray(3) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = i match {
      case 0 =>
        val result = FurnaceRecipes.instance().getSmeltingResult(Converter.ItemStackFromIItemStack(stack))
        result != null && !result.isEmpty
      case 2 =>
        stack.isEmpty || stack.hasModule(ManagerModules.POWER_STORAGE, null)
      case _ => false
    }
  }
  val inputStorage  : IItemStorage = new ItemStorageSlice(storage, Array(0))
  val outputStorage : IItemStorage = new ItemStorageSlice(storage, Array(1))
  val batteryStorage: IItemStorage = new ItemStorageSlice(storage, Array(2))
  val battery       : IBattery     = new DynamicIBattery(() => batteryStorage.head.moduleOption(ManagerModules.POWER_STORAGE, null).getOrElse(IBattery.Empty))
  val sidedStorageConfig           = new SidedItemStorageConfiguration({
    case EnumFacing.UP | EnumFacing.SOUTH => INPUT_INV_KEY
    case EnumFacing.DOWN | EnumFacing.EAST | EnumFacing.WEST | EnumFacing.NORTH => OUTPUT_INV_KEY
    case _ => NONE_INV_KEY
  },
   Map(NONE_INV_KEY -> IItemStorage.Empty,
       INPUT_INV_KEY -> inputStorage,
       OUTPUT_INV_KEY -> outputStorage),
   () => world.getBlockState(pos).getValue(BlockBehaviorHorizontalFacing.FACING))

  val internal = new CrystalFurnaceModule(inputStorage, outputStorage, batteryStorage, battery)

  addTileEntityModule(new ModuleIItemStorage(storage))
  addTileEntityModule(new ModuleIItemHandlerConverter)
  addTileEntityModule(new ModuleIItemSidedConfiguration(sidedStorageConfig))
  addTileEntityModule(new ModuleDropInventory(storage))
  addTileEntityModule(new ModuleIBatteryFromItemStack(batteryStorage.head _))
  addTileEntityModule(new ModuleGui(Femtocraft, GuiIDs.TileCrystalFurnaceID _))
  addTileEntityModule(new ModuleWiredPowerLeafNode(this, battery _, PowerStorageNodeType.CONSUMER, () => 50d))
  addTileEntityModuleTickable(new ModuleColorableFromICrystal(this, () => batteryStorage.head.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null)))
  addTileEntityModuleTickable(new ModuleIItemAutoIO(sidedStorageConfig))
  addTileEntityModuleTickable(internal)
}

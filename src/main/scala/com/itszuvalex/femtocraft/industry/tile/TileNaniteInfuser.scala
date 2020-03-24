package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.nanite.{INaniteTank, NaniteTank}
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.industry.tile.TileNaniteInfuser._
import com.itszuvalex.femtocraft.industry.{ModuleINaniteTank, ModuleNaniteAutoIO, ModuleNaniteSidedConfiguration, NaniteInfusionRecipeRegistry}
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration
import com.itszuvalex.femtocraft.power.{ModuleColorableFromPowerLeafNode, ModulePowerLeafNode, ModulePowerStorage, ModulePowerStorageNodeFromPowerLeafNode}
import com.itszuvalex.femtocraft.{FemtoBlocks, Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.{IModule, Module}
import com.itszuvalex.itszulib.api.storage._
import com.itszuvalex.itszulib.api.wrappers.{Converter, IBlock, IItemStack}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviorHorizontalFacing
import com.itszuvalex.itszulib.core.modules._
import com.itszuvalex.itszulib.core.{SidedItemStorageConfiguration, TileEntityCoreTickable}
import net.minecraft.util.EnumFacing

object TileNaniteInfuser {
  val MODULE: IModule[NaniteInfuserModule] = Module.registerModule("NaniteInfuserModule", null)

  val INPUT_INV_KEY  = "Input"
  val OUTPUT_INV_KEY = "Output"
  val NONE_INV_KEY   = "None"

  val NANITE_TANK_KEY = "Tank"
  val NONE_TANK_KEY   = "None"

  val TASK_NBT                = "Task"
  val ITEM_SIDED_CONFIG_NBT   = "ItemConfig"
  val NANITE_SIDED_CONFIG_NBT = "NaniteConfig"
  val TICKS_NBT               = "Ticks"

  val TICKS_FOR_AUTOIO = 20
  val AMT_PER_AUTOIO   = 1
  val VOL_PER_AUTOIO   = 1
}

class TileNaniteInfuser extends TileEntityCoreTickable {
  val storage      : IItemStorage = new ItemStorageArray(2) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = {
      (i, stack) match {
        case (_, null) => true
        case (_, b) if b.isEmpty => true
        case (0, _) => NaniteInfusionRecipeRegistry.getMatchingRecipe(stack).isDefined
        case _ => false
      }
    }
  }
  val inputStorage : IItemStorage = new ItemStorageSlice(storage, Array(0))
  val outputStorage: IItemStorage = new ItemStorageSlice(storage, Array(1))
  val sidedStorageConfig          = new SidedItemStorageConfiguration({
    case EnumFacing.UP | EnumFacing.SOUTH => INPUT_INV_KEY
    case EnumFacing.DOWN | EnumFacing.EAST | EnumFacing.WEST | EnumFacing.NORTH => OUTPUT_INV_KEY
    case _ => NONE_INV_KEY
  },
   Map(NONE_INV_KEY -> IItemStorage.Empty,
       INPUT_INV_KEY -> inputStorage,
       OUTPUT_INV_KEY -> outputStorage),
   () => world.getBlockState(pos).getValue(BlockBehaviorHorizontalFacing.FACING))
  val naniteTank   : INaniteTank  = new NaniteTank(50)
  val sidedNaniteConfig           = new SidedNaniteStorageConfiguration(_ => NANITE_TANK_KEY,
                                                                        Map(NONE_TANK_KEY -> INaniteTank.Empty,
                                                                            NANITE_TANK_KEY -> naniteTank),
                                                                        () => world.getBlockState(pos).getValue(BlockBehaviorHorizontalFacing.FACING))
  val battery      : IBattery     = new PowerBattery(4000)
  val leafNode                    = new ModulePowerLeafNode(this, battery, PowerStorageNodeType.CONSUMER, 8, () => 50d)

  val internal = new NaniteInfuserModule(inputStorage, outputStorage, battery, naniteTank)

  addTileEntityModule(new ModuleIItemStorage(storage))
  addTileEntityModule(new ModuleIItemHandlerConverter)
  addTileEntityModule(new ModuleIItemSidedConfiguration(sidedStorageConfig))
  addTileEntityModule(new ModuleDropInventory(storage))
  addTileEntityModule(new ModulePowerStorage(battery))
  addTileEntityModule(leafNode)
  addTileEntityModule(new ModuleINaniteTank(naniteTank))
  addTileEntityModule(new ModuleNaniteSidedConfiguration(sidedNaniteConfig))
  addTileEntityModule(new ModulePowerStorageNodeFromPowerLeafNode(leafNode))
  addTileEntityModule(new ModuleColorableFromPowerLeafNode(leafNode))
  addTileEntityModuleTickable(new ModuleIItemAutoIO(sidedStorageConfig))
  addTileEntityModuleTickable(new ModuleNaniteAutoIO(sidedNaniteConfig))
  addTileEntityModuleTickable(internal)

  override def hasDescription: Boolean = true

  override def getMod: AnyRef = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileNaniteInfuserID

  // TODO
  override def getBlock: IBlock = Converter.IBlockFromBlock(FemtoBlocks.blockNaniteInfuser)
}

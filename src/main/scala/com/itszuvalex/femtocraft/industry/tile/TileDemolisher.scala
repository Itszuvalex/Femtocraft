package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.industry.DustRecipeRegistry
import com.itszuvalex.femtocraft.industry.tile.TileDemolisher._
import com.itszuvalex.femtocraft.power.{ModuleColorableFromPowerLeafNode, ModulePowerLeafNode, ModulePowerStorageNodeFromPowerLeafNode}
import com.itszuvalex.femtocraft.temp.{ModuleIItemAutoIO, ModuleIItemSidedConfiguration}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.{IModule, Module}
import com.itszuvalex.itszulib.api.storage._
import com.itszuvalex.itszulib.api.wrappers.{IItemStack, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviorHorizontalFacing
import com.itszuvalex.itszulib.core.modules.{ModuleDropInventory, ModuleIItemHandlerConverter, ModuleIItemStorage}
import com.itszuvalex.itszulib.core.{SidedItemStorageConfiguration, TileEntityCoreTickable}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}

object TileDemolisher {
  val MODULE: IModule[DemolisherModule] = Module.registerModule[DemolisherModule]("DemolisherModule", null)

  val INPUT_INV_KEY  = "Input"
  val OUTPUT_INV_KEY = "Output"
  val NONE_INV_KEY   = "None"
}

class TileDemolisher extends TileEntityCoreTickable {
  val storage      : IItemStorage = new ItemStorageArray(2) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = i match {
      case 0 =>
        val result = DustRecipeRegistry.getDust(stack).getOrElse(IItemStack.Empty)
        result != null && !result.isEmpty
      case _ => false
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
  val battery      : IBattery     = new PowerBattery(5000)
  val leafNode                    = new ModulePowerLeafNode(this, battery, PowerStorageNodeType.CONSUMER, transRate = () => 50d)

  val internal = new DemolisherModule(inputStorage, outputStorage, battery)

  addTileEntityModule(new ModuleIItemStorage(storage))
  addTileEntityModule(new ModuleIItemHandlerConverter)
  addTileEntityModule(new ModuleIItemSidedConfiguration(sidedStorageConfig))
  addTileEntityModule(new ModuleDropInventory)
  addTileEntityModule(leafNode)
  addTileEntityModule(new ModulePowerStorageNodeFromPowerLeafNode(leafNode))
  addTileEntityModule(new ModuleColorableFromPowerLeafNode(leafNode))
  addTileEntityModuleTickable(new ModuleIItemAutoIO(sidedStorageConfig))
  addTileEntityModuleTickable(internal)

  override def getMod: AnyRef = Femtocraft

  override def hasDescription: Boolean = true

  override def hasGUI = true

  override def getGuiID: Int = GuiIDs.TileDemolisherGuiID

  override def onBlockActivated(world: IWorld, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = false
}

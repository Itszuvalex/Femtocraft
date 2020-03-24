package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace._
import com.itszuvalex.femtocraft.power.{ModuleColorableFromPowerLeafNode, ModulePowerLeafNode, ModulePowerStorage, ModulePowerStorageNodeFromPowerLeafNode}
import com.itszuvalex.femtocraft.{FemtoBlocks, Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.{IModule, Module}
import com.itszuvalex.itszulib.api.storage.{ItemStorageArray, _}
import com.itszuvalex.itszulib.api.wrappers.{Converter, IBlock, IItemStack}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviorHorizontalFacing
import com.itszuvalex.itszulib.core.modules._
import com.itszuvalex.itszulib.core.{SidedItemStorageConfiguration, TileEntityCoreTickable}
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.util.EnumFacing

/**
  * Created by Chris on 8/14/2016.
  */

object TileNanoFurnace {
  val MODULE: IModule[NanoFurnaceModule] = Module.registerModule[NanoFurnaceModule]("NanoFurnaceModule", null)

  val INPUT_INV_KEY  = "Input"
  val OUTPUT_INV_KEY = "Output"
  val NONE_INV_KEY   = "None"

  /*
  val FRONT_TEX_BASE = Resources.TexBlock("blockmachineblock_front_base.png")
  val FRONT_TEX_COLOR = Resources.TexBlock("blockmachineblock_front_color.png")
  val FRONT_TEX_ADD = Resources.TexBlock("nanofurnace_front.png")
  val SIDE_TEX_BASE = Resources.TexBlock("blockmachineblock_side_base.png")
  val SIDE_TEX_COLOR = Resources.TexBlock("blockmachineblock_side_color.png")
  val SIDE_TEX_EMPTY = Resources.TexBlock("blockmachineblock_side_empty.png")
  val SIDE_TEX_EMPTY_LIGHT = Resources.TexBlock("blockmachineblock_side_empty_light.png")
  */

}

class TileNanoFurnace extends TileEntityCoreTickable {
  val storage      : IItemStorage = new ItemStorageArray(2) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = i match {
      case 0 =>
        val result = FurnaceRecipes.instance().getSmeltingResult(Converter.ItemStackFromIItemStack(stack))
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

  val internal = new NanoFurnaceModule(inputStorage, outputStorage, battery)

  addTileEntityModule(new ModuleIItemStorage(storage))
  addTileEntityModule(new ModuleIItemHandlerConverter)
  addTileEntityModule(new ModuleIItemSidedConfiguration(sidedStorageConfig))
  addTileEntityModule(new ModuleDropInventory(storage))
  addTileEntityModule(new ModulePowerStorage(battery))
  addTileEntityModule(leafNode)
  addTileEntityModule(new ModulePowerStorageNodeFromPowerLeafNode(leafNode))
  addTileEntityModule(new ModuleColorableFromPowerLeafNode(leafNode))
  addTileEntityModuleTickable(new ModuleIItemAutoIO(sidedStorageConfig))
  addTileEntityModuleTickable(internal)

  override def getMod: AnyRef = Femtocraft

  override def hasDescription: Boolean = true

  override def hasGUI = true

  override def getGuiID: Int = GuiIDs.TileFurnaceGuiID

  //TODO: Remove this once ItszuLib bumped again
  override def getBlock: IBlock = Converter.IBlockFromBlock(FemtoBlocks.blockNanoFurnace)
}

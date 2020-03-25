package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.nanite.{INaniteTank, NaniteTank}
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.industry.tile.TileNaniteExtractor._
import com.itszuvalex.femtocraft.industry.{ModuleINaniteTank, ModuleNaniteAutoIO, ModuleNaniteSidedConfiguration}
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration
import com.itszuvalex.femtocraft.power.{ModuleColorableFromPowerLeafNode, ModulePowerLeafNode, ModulePowerStorage, ModulePowerStorageNodeFromPowerLeafNode}
import com.itszuvalex.femtocraft.{FemtoBlocks, Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.{IModule, Module}
import com.itszuvalex.itszulib.api.storage.{IBattery, IItemStorage, ItemStorageArray, PowerBattery}
import com.itszuvalex.itszulib.api.wrappers.{Converter, IBlock, IItemStack, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviorHorizontalFacing
import com.itszuvalex.itszulib.core.modules._
import com.itszuvalex.itszulib.core.{SidedItemStorageConfiguration, TileEntityCoreTickable}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}

object TileNaniteExtractor {
  val MODULE: IModule[NaniteExtractorModule] = Module.registerModule("NaniteExtractorModule", null)

  val INPUT_INV_KEY = "Input"
  val NONE_KEY      = "None"

  val NANITE_TANK_KEY = "Tank"
  val NONE_TANK_KEY   = "None"
}

class TileNaniteExtractor extends TileEntityCoreTickable {
  val storage           : IItemStorage                    = new ItemStorageArray(1) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = {
      CybermaterialRegistry.getNaniteFromItem(stack.item, stack.damage).isDefined
    }
  }
  val sidedStorageConfig: SidedItemStorageConfiguration   = new SidedItemStorageConfiguration({
    case EnumFacing.UP | EnumFacing.SOUTH => INPUT_INV_KEY
    case _ => NONE_KEY
  },
   Map(NONE_KEY -> IItemStorage.Empty,
       INPUT_INV_KEY -> storage),
   () => world.getBlockState(pos).getValue(BlockBehaviorHorizontalFacing.FACING))
  val naniteTank        : INaniteTank                     = new NaniteTank(50)
  val sidedNaniteConfig : SidedNaniteStorageConfiguration = new SidedNaniteStorageConfiguration(_ => NANITE_TANK_KEY,
                                                                                                Map(NONE_TANK_KEY -> null,
                                                                                                    NANITE_TANK_KEY -> naniteTank),
                                                                                                () => world.getBlockState(pos).getValue(BlockBehaviorHorizontalFacing.FACING))
  val battery           : IBattery                        = new PowerBattery(5000)
  val leafNode                                            = new ModulePowerLeafNode(this, battery, PowerStorageNodeType.CONSUMER, transRate = () => 80d)

  val internal = new NaniteExtractorModule(storage, battery, naniteTank)

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

  override def getMod: AnyRef = Femtocraft

  override def hasGUI = true

  override def getGuiID: Int = GuiIDs.TileNaniteExtractorID

  // TODO
  override def onBlockActivated(world: IWorld, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    val ret = super.onBlockActivated(world, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)
    if (!ret && hasGUI) {
      playerIn.openGui(getMod, getGuiID, getWorld, getPos.getX, getPos.getY, getPos.getZ)
      return true
    }
    ret
  }
}

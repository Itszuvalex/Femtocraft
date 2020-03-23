package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository._
import com.itszuvalex.femtocraft.temp.{ModuleIItemAutoIO, ModuleIItemSidedConfiguration}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.IWorld
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviorHorizontalFacing
import com.itszuvalex.itszulib.core.modules.{ModuleIItemHandlerConverter, ModuleIItemStorage}
import com.itszuvalex.itszulib.core.{SidedItemStorageConfiguration, TileEntityCoreTickable}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/15.
  */
object TileItemRepository {
  val INVENTORY_SIZE = 9 * 6
  val INV_KEY        = "Inventory"
  val NONE_KEY       = "None"
}

class TileItemRepository extends TileEntityCoreTickable {
  val storage: IItemStorage = new ItemStorageArray(INVENTORY_SIZE)
  val sidedStorageConfig    = new SidedItemStorageConfiguration(_ => INV_KEY,
                                                                Map(NONE_KEY -> IItemStorage.Empty,
                                                                    INV_KEY -> storage),
                                                                () => world.getBlockState(pos).getValue(BlockBehaviorHorizontalFacing.FACING))

  addTileEntityModule(new ModuleIItemStorage(storage))
  addTileEntityModule(new ModuleIItemHandlerConverter)
  addTileEntityModule(new ModuleIItemSidedConfiguration(sidedStorageConfig))
  addTileEntityModuleTickable(new ModuleIItemAutoIO(sidedStorageConfig))

  override def getMod = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileItemRepositoryGuiID

  // TODO

  override def hasDescription: Boolean = false

  override def onBlockActivated(world: IWorld, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = false
}

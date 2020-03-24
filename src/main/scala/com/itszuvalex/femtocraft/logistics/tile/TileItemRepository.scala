package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository._
import com.itszuvalex.femtocraft.{FemtoBlocks, Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.{Converter, IBlock}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviorHorizontalFacing
import com.itszuvalex.itszulib.core.modules._
import com.itszuvalex.itszulib.core.{SidedItemStorageConfiguration, TileEntityCoreTickable}

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
  addTileEntityModule(new ModuleDropInventory(storage))
  addTileEntityModuleTickable(new ModuleIItemAutoIO(sidedStorageConfig))

  override def getMod = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileItemRepositoryGuiID

  override def hasDescription: Boolean = false

  // TODO
  override def getBlock: IBlock = Converter.IBlockFromBlock(FemtoBlocks.blockItemRepository)
}

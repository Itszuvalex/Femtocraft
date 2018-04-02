package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository._
import com.itszuvalex.femtocraft.util.TileEntityUtils
import com.itszuvalex.femtocraft.util.data.{DataInt, DataSerializable, TileDataSpec}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.core.traits.tile.{BlockFacing, TileInventory}
import com.itszuvalex.itszulib.core.{SidedItemStorageConfiguration, TileEntityBase}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/15.
  */
object TileItemRepository {
  val INVENTORY_SIZE         = 9 * 6
  val HIVE_CONNECTION_RADIUS = 32f
  val TICKS_FOR_AUTOIO       = 20
  val AMT_FOR_AUTOIO         = 1
  val TICKS_NBT              = "Ticks"
  val ITEM_SIDED_CONFIG_NBT  = "ItemConfig"
  val INV_KEY                = "Inventory"
  val NONE_KEY               = "None"
}

class TileItemRepository extends TileEntityBase with TileInventory with TileDataSpec {
  private val sidedStorageConfig = new SidedItemStorageConfiguration(_ => INV_KEY,
    Map(NONE_KEY -> IItemStorage.Empty,
      INV_KEY -> storage),
    () => world.getBlockState(pos).getValue(BlockFacing.FACING))
  var ticks = 0

  descriptionDataSpec += new DataSerializable[NBTTagCompound](ITEM_SIDED_CONFIG_NBT, sidedStorageConfig)
  saveDataSpec ++= Array(
    new DataSerializable[NBTTagCompound](ITEM_SIDED_CONFIG_NBT, sidedStorageConfig),
    new DataInt(TICKS_NBT, ticks _, ticks_=)
  )

  override def defaultStorage: IItemStorage = new ItemStorageArray(INVENTORY_SIZE)

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = (capability, facing) match {
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.ITEM_STORAGE_CONFIGURABLE => true
    case (_, null) => super.hasCapability(capability, facing)
    case (cap, _) if cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => true
    case (cap, _) if cap == ItszuLibCapabilities.ITEM_STORAGE => true
    case _ => super.hasCapability(capability, facing)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = (capability, facing) match {
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.ITEM_STORAGE_CONFIGURABLE => sidedStorageConfig.asInstanceOf[T]
    case (_, null) => super.getCapability(capability, facing)
    case (cap, _) if cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => Converter.IItemHandlerModifiableFromIItemStorage(sidedStorageConfig.getStorageForGlobalFacing(facing)).asInstanceOf[T]
    case (cap, _) if cap == ItszuLibCapabilities.ITEM_STORAGE => sidedStorageConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case _ => super.getCapability(capability, facing)
  }

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    ticks = TileEntityUtils.incrementTicks(ticks, TICKS_FOR_AUTOIO)
    TileEntityUtils.checkDoItemInputIO(this, sidedStorageConfig, ticks, AMT_FOR_AUTOIO)
    TileEntityUtils.checkDoItemOutputIO(this, sidedStorageConfig, ticks, AMT_FOR_AUTOIO)
  }

  override def getMod = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileItemRepositoryGuiID
}

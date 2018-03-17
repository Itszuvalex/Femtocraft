package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.logistics.storage.item.{IIndexedInventory, IndexedInventory}
import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository._
import com.itszuvalex.femtocraft.util.TileEntityUtils
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.Capabilities
import com.itszuvalex.itszulib.api.core.Saveable
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageInventory}
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.core.traits.tile.BlockFacing
import com.itszuvalex.itszulib.core.{SidedItemStorageConfiguration, TileEntityBase}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler

import scala.collection.mutable

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

class TileItemRepository extends TileEntityBase with IIndexedInventory with IInventory {
  @Saveable val indInventory: IndexedInventory = new IndexedInventory(INVENTORY_SIZE)
            val storage     : IItemStorage     = new ItemStorageInventory(indInventory)
  private val sidedStorageConfig = new SidedItemStorageConfiguration(_ => INV_KEY,
    Map(NONE_KEY -> IItemStorage.Empty,
      INV_KEY -> storage),
    () => world.getBlockState(pos).getValue(BlockFacing.FACING))
  var ticks = 0

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = (capability, facing) match {
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.ITEM_STORAGE_CONFIGURABLE => true
    case (_, null) => false
    case (cap, _) if cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => true
    case (cap, _) if cap == Capabilities.ITEM_STORAGE => true
    case _ => false
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = (capability, facing) match {
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.ITEM_STORAGE_CONFIGURABLE => sidedStorageConfig.asInstanceOf[T]
    case (_, null) => null.asInstanceOf[T]
    case (cap, _) if cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => Converter.IItemHandlerModifiableFromIItemStorage(sidedStorageConfig.getStorageForGlobalFacing(facing)).asInstanceOf[T]
    case (cap, _) if cap == Capabilities.ITEM_STORAGE => sidedStorageConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case _ => null.asInstanceOf[T]
  }

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    ticks = TileEntityUtils.checkDoItemInputIO(this, sidedStorageConfig, ticks, TICKS_FOR_AUTOIO, AMT_FOR_AUTOIO)
    TileEntityUtils.checkDoItemOutputIO(this, sidedStorageConfig, ticks, AMT_FOR_AUTOIO)
  }

  override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(nbt)
    nbt.setInteger(TICKS_NBT, ticks)
    nbt.setTag(ITEM_SIDED_CONFIG_NBT, sidedStorageConfig.serializeNBT())
    nbt
  }

  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)
    ticks = nbt.getInteger(TICKS_NBT)
    if (nbt.hasKey(ITEM_SIDED_CONFIG_NBT))
      sidedStorageConfig.deserializeNBT(nbt.getCompoundTag(ITEM_SIDED_CONFIG_NBT))
  }

  override def getMod = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileItemRepositoryGuiID

  /**
    * @return ItemStack[] that backs this inventory class. Modifications to it modify this.
    */
  def getInventory: Array[ItemStack] = indInventory.getInventory

  override def getSizeInventory: Int = indInventory.getSizeInventory

  override def getStackInSlot(i: Int): ItemStack = indInventory.getStackInSlot(i)

  override def decrStackSize(i: Int, amount: Int): ItemStack = {
    setModified()
    indInventory.decrStackSize(i, amount)
  }

  override def setInventorySlotContents(i: Int, itemstack: ItemStack): Unit = {
    setModified()
    indInventory.setInventorySlotContents(i, itemstack)
  }

  override def addItemStack(itemStack: ItemStack, slot: Int): Unit = {
    setModified()
    indInventory.addItemStack(itemStack, slot)
  }

  override def removeItemStack(slot: Int): Unit = {
    setModified()
    indInventory.removeItemStack(slot)
  }

  override def closeInventory(player: EntityPlayer): Unit = indInventory.closeInventory(player)

  override def clear(): Unit = indInventory.clear()

  override def openInventory(player: EntityPlayer): Unit = indInventory.openInventory(player)

  override def getFieldCount: Int = indInventory.getFieldCount

  override def getField(id: Int): Int = indInventory.getField(id)

  override def removeStackFromSlot(index: Int): ItemStack = indInventory.removeStackFromSlot(index)

  override def setField(id: Int, value: Int): Unit = indInventory.setField(id, value)

  override def getName: String = indInventory.getName

  override def hasCustomName: Boolean = indInventory.hasCustomName

  override def getInventoryStackLimit: Int = indInventory.getInventoryStackLimit

  override def markDirty(): Unit = indInventory.markDirty()

  override def isUsableByPlayer(entityplayer: EntityPlayer): Boolean = indInventory.isUsableByPlayer(entityplayer)

  override def isItemValidForSlot(i: Int, itemstack: ItemStack): Boolean = indInventory.isItemValidForSlot(i, itemstack)

  override def invalidateCache(): Unit = indInventory.invalidateCache()

  override def getSlotsByOreID(id: Int): Option[mutable.HashSet[Int]] = indInventory.getSlotsByOreID(id)

  override def containsItemStack(itemStack: ItemStack): Boolean = indInventory.containsItemStack(itemStack)

  override def getSlotsByItemStack(itemStack: ItemStack): Option[mutable.HashSet[Int]] = indInventory.getSlotsByItemStack(itemStack)

  override def getSlotsByOreName(name: String): Option[mutable.HashSet[Int]] = indInventory.getSlotsByOreName(name)

  override def containsOre(ore: String): Boolean = indInventory.containsOre(ore)

  override def rebuildCache(): Unit = indInventory.rebuildCache()

  override def getSlotsByItemID(id: Int): Option[mutable.HashSet[Int]] = indInventory.getSlotsByItemID(id)

  override def isCacheValid: Boolean = indInventory.isCacheValid

  override def rebuildCacheIfNecessary(): Unit = indInventory.rebuildCacheIfNecessary()

  override def getContainedOres: collection.Set[String] = indInventory.getContainedOres

  override def getContainedIDs: collection.Set[Int] = indInventory.getContainedIDs

  override def hasDescription: Boolean = false

  override def isEmpty: Boolean = indInventory.isEmpty
}

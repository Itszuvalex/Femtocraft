package com.itszuvalex.femtocraft.logistics.storage.item

import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import net.minecraft.item.ItemStack

import scala.collection.{Set, mutable}

/**
  * Created by Christopher on 8/29/2015.
  */
trait TileMultiblockIndexedInventory extends TileEntityBase with TileIndexedInventory {
  self: MultiBlockComponent =>

  override def addItemStack(itemStack: ItemStack, slot: Int): Unit =
    if (isController) super.addItemStack(itemStack, slot) else forwardToController[TileMultiblockIndexedInventory, Unit](_.addItemStack(itemStack, slot))

  override def removeItemStack(itemStack: ItemStack, slot: Int): Unit =
    if (isController) super.removeItemStack(itemStack, slot) else forwardToController[TileMultiblockIndexedInventory, Unit](_.removeItemStack(itemStack, slot))

  override def getSlotsByOreID(id: Int): Option[mutable.HashSet[Int]] =
    if (isController) super.getSlotsByOreID(id) else forwardToController[TileMultiblockIndexedInventory, Option[mutable.HashSet[Int]]](_.getSlotsByOreID(id))

  override def containsItemStack(itemStack: ItemStack): Boolean =
    if (isController) super.containsItemStack(itemStack) else forwardToController[TileMultiblockIndexedInventory, Boolean](_.containsItemStack(itemStack))

  override def containsOre(ore: String): Boolean =
    if (isController) super.containsOre(ore) else forwardToController[TileMultiblockIndexedInventory, Boolean](_.containsOre(ore))

  override def getSlotsByOreName(name: String): Option[mutable.HashSet[Int]] =
    if (isController) super.getSlotsByOreName(name) else forwardToController[TileMultiblockIndexedInventory, Option[mutable.HashSet[Int]]](_.getSlotsByOreName(name))

  override def getSlotsByItemStack(itemStack: ItemStack): Option[mutable.HashSet[Int]] =
    if (isController) super.getSlotsByItemStack(itemStack) else forwardToController[TileMultiblockIndexedInventory, Option[mutable.HashSet[Int]]](_.getSlotsByItemStack(itemStack))

  override def rebuildCache(): Unit =
    if (isController) super.rebuildCache() else forwardToController[TileMultiblockIndexedInventory, Unit](_.rebuildCache())

  override def getSlotsByItemID(id: Int): Option[mutable.HashSet[Int]] =
    if (isController) super.getSlotsByItemID(id) else forwardToController[TileMultiblockIndexedInventory, Option[mutable.HashSet[Int]]](_.getSlotsByItemID(id))

  override def isCacheValid: Boolean =
    if (isController) super.isCacheValid else forwardToController[TileMultiblockIndexedInventory, Boolean](_.isCacheValid)

  override def invalidateCache(): Unit =
    if (isController) super.invalidateCache() else forwardToController[TileMultiblockIndexedInventory, Unit](_.invalidateCache())

  override def rebuildCacheIfNecessary(): Unit =
    if (isController) super.rebuildCacheIfNecessary() else forwardToController[TileMultiblockIndexedInventory, Unit](_.rebuildCacheIfNecessary())

  override def getContainedOres: Set[String] =
    if (isController) super.getContainedOres else forwardToController[TileMultiblockIndexedInventory, Set[String]](_.getContainedOres)

  override def getContainedIDs: Set[Int] =
    if (isController) super.getContainedIDs else forwardToController[TileMultiblockIndexedInventory, Set[Int]](_.getContainedIDs)

}

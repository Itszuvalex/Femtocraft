package com.itszuvalex.femtocraft.logistics.storage.item

import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import net.minecraft.item.ItemStack

import scala.collection.Set

/**
 * Created by Christopher on 8/29/2015.
 */
trait TileMultiblockIndexedInventory extends TileEntityBase with TileIndexedInventory {
  self: MultiBlockComponent =>

  override def addItemStack(itemStack: ItemStack, slot: Int): Unit =
    if (isController) super.addItemStack(itemStack, slot) else forwardToController(_.addItemStack(itemStack, slot))

  override def removeItemStack(itemStack: ItemStack, slot: Int) =
    if (isController) super.removeItemStack(itemStack, slot) else forwardToController(_.removeItemStack(itemStack, slot))

  override def getSlotsByOreID(id: Int) =
    if (isController) super.getSlotsByOreID(id) else forwardToController(_.getSlotsByOreID(id))

  override def containsItemStack(itemStack: ItemStack) =
    if (isController) super.containsItemStack(itemStack) else forwardToController(_.containsItemStack(itemStack))

  override def containsOre(ore: String) =
    if (isController) super.containsOre(ore) else forwardToController(_.containsOre(ore))

  override def getSlotsByOreName(name: String) =
    if (isController) super.getSlotsByOreName(name) else forwardToController(_.getSlotsByOreName(name))

  override def getSlotsByItemStack(itemStack: ItemStack) =
    if (isController) super.getSlotsByItemStack(itemStack) else forwardToController(_.getSlotsByItemStack(itemStack))

  override def rebuildCache() =
    if (isController) super.rebuildCache() else forwardToController(_.rebuildCache())

  override def getSlotsByItemID(id: Int) =
    if (isController) super.getSlotsByItemID(id) else forwardToController(_.getSlotsByItemID(id))

  override def isCacheValid =
    if (isController) super.isCacheValid else forwardToController(_.isCacheValid)

  override def invalidateCache() =
    if (isController) super.invalidateCache() else forwardToController(_.invalidateCache())

  override def rebuildCacheIfNecessary(): Unit =
    if (isController) super.rebuildCacheIfNecessary() else forwardToController(_.rebuildCacheIfNecessary())

  override def getContainedOres: Set[String] =
    if (isController) super.getContainedOres else forwardToController(_.getContainedOres)

  override def getContainedIDs: Set[Int] =
    if (isController) super.getContainedIDs else forwardToController(_.getContainedIDs)

}

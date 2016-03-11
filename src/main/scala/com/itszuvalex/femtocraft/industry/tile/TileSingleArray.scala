package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.industry.tile.TileSingleArray._
import com.itszuvalex.femtocraft.logistics.IItemLogisticsNetwork
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileIndexedInventoryWithIInventory}
import com.itszuvalex.femtocraft.power.node.PowerNode
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.util.Comparators.ItemStack.IDDamageWildCardNBTComparator
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 3/9/16.
  */
object TileSingleArray {
  val inventorySize = 4
  val indexInput    = 0
  val indexOutput   = 1
  val indexAssembly = 3
  val indexPower    = 4
}

class TileSingleArray extends TileEntityBase with ITileAssemblyArray with PowerNode with TileIndexedInventoryWithIInventory {

  override def getMod: AnyRef = Femtocraft

  /**
    *
    * @return Base Time usage * getTimeMultiplier = actual time requirement.  Assemblies are responsible for calculating the time usage themselves.
    */
  override def getTimeMultiplier: Double = 1

  /**
    *
    * @param slot (0 until getOutputSlots)
    * @param amt  Amount of item from said slot to remove.
    * @return The itemstack consisting of getOutputItem(slot) and of stack size Math.min(getOutputItem(slot).stackSize, amt), or null if no item in slot.
    */
  override def removeOutputItem(slot: Int, amt: Int): ItemStack = {
    val item = getOutputItem(slot)
    if (item != null) {
      val remove = Math.min(item.stackSize, amt)
      item.stackSize -= remove
      if (item.stackSize == 0) {
        indInventory.setInventorySlotContents(indexOutput + slot, null)
      }
      val ret = item.copy()
      ret.stackSize = remove
      ret
    }
    else null
  }

  /**
    *
    * @param item Item to merge into slot.
    * @param slot (0 until getInputSlots)
    * @return Remainder of item after the add or merge.  Should only be non-null if item doesn't match getInputItem(slot), or not enough space.
    */
  override def addOrMergeInputItem(item: ItemStack, slot: Int): ItemStack = {
    if (item == null) return null

    val slotItem = getInputItem(slot)
    if (slotItem == null) {
      indInventory.setInventorySlotContents(indexInput + slot, slotItem)
      null
    }
    else {
      if (IDDamageWildCardNBTComparator.compare(item, slotItem) == 0) {
        val room = slotItem.getMaxStackSize - slotItem.stackSize
        val amount = Math.min(room, item.stackSize)
        slotItem.stackSize += amount
        if (room > 0 && amount <= room) {
          item.stackSize -= amount
          if (item.stackSize == 0)
            null
          else
            item
        }
        else {
          item.stackSize -= amount
          if (item.stackSize == 0)
            null
          else
            item
        }
      }
      else {
        item
      }
    }
  }

  /**
    *
    * @param item Item to merge into slot.
    * @param slot (0 until getOutputSlots)
    * @return Remainder of item after the add or merge.  Should only be non-null if item doesn't match getOutputItem(slot), or not enough space.
    */
  override def addOrMergeOutputItem(item: ItemStack, slot: Int): ItemStack = {
    if (item == null) return null
    if (item.stackSize == 0) return null

    val slotItem = getOutputItem(slot)
    if (slotItem == null) {
      indInventory.setInventorySlotContents(indexOutput + slot, item)
      null
    }
    else {
      if (IDDamageWildCardNBTComparator.compare(item, slotItem) == 0) {
        val room = slotItem.getMaxStackSize - slotItem.stackSize
        val amount = Math.min(room, item.stackSize)
        slotItem.stackSize += amount
        if (room > 0 && amount <= room) {
          item.stackSize -= amount
          if (item.stackSize == 0)
            null
          else
            item
        }
        else {
          item.stackSize -= amount
          if (item.stackSize == 0)
            null
          else
            item
        }
      }
      else {
        item
      }
    }
  }

  /**
    *
    * @return Base Power usage * getPowerMultipler = actual power requirement.  Assemblies are responsible for calculating their power usage before draining power.
    */
  override def getPowerMultiplier: Double = ???

  /**
    *
    * @param slot (0 until getAssemblySlots) to remove from.
    * @return The assembly item stack in given and now empty slot, or null if failed to remove assembly.  (somehow?)
    */
  override def removeAssembly(slot: Int): ItemStack = ???

  /**
    * Number of assembly slots
    */
  override def getAssemblySlots: Int = ???

  /**
    *
    * @return Set of support Assembly types
    */
  override def getSupportedAssemblyTypes: Set[String] = ???

  /**
    *
    * @param slot (0 until getInputSlots)
    * @param amt  Amount of the item from said slot to remove.
    * @return The itemstack consisting of getInputItem(slot) and of stack size Math.min(getInputItem(slot).stackSize, amt), or null if no item in slot.
    */
  override def removeInputItem(slot: Int, amt: Int): ItemStack = ???

  /**
    *
    * @param slot (0 until getInputSlots)
    * @return Itemstack in given input slot.
    */
  override def getInputItem(slot: Int): ItemStack = ???

  /**
    *
    * @param slot (0 until getOutputSlots)
    * @return Itemstack in given slot.
    */
  override def getOutputItem(slot: Int): ItemStack = ???

  /**
    *
    * @return Number of slots that are accessible for given IItemAssemblies to output to.
    */
  override def getOutputSlots: Int = ???

  /**
    *
    * @param assembly Assembly to insert into slot.  Should not be null.
    * @param slot     (0 until getAssemblySlots) to insert into.
    * @return True if slot is empty and assembly was valid, accepted, and placed in the slot.
    */
  override def addAssembly(assembly: ItemStack, slot: Int): Boolean = ???

  /**
    *
    * @param slot (0 until getAssemblySlots)
    * @return IItemAssembly in the given slot.
    */
  override def getAssembly(slot: Int): ItemStack = ???

  /**
    *
    * @return Number of slots that are accessible for given IItemAssemblies to withdraw from.
    */
  override def getInputSlots: Int = ???

  /**
    *
    * @return IItemLogisticsNetwork connection, or null if no logistics supported.
    */
  override def getItemLogisticsNetwork: IItemLogisticsNetwork = ???

  override def defaultInventory: IndexedInventory = ???

  override def hasDescription: Boolean = ???

  /**
    *
    * @return The type of PowerNode this is.
    */
  override def getType: String = ???

  /**
    *
    * @param amt      Amount to attempt to charge
    * @param doCharge False to simulate, true to actually do
    * @return Amount of amt used to actually charge.
    */
  override def charge(amt: Double, doCharge: Boolean): Double = ???

  /**
    *
    * @param amt     Amount of power to drain
    * @param doDrain False to simulate, true to actually remove power.
    * @return Amount of amt that was successfully drained.
    */
  override def drain(amt: Double, doDrain: Boolean): Double = ???

  override def getCurrentPower: Double = ???

  override def getMaximumPower: Double = ???
}

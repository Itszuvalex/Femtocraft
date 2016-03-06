package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.logistics.IItemLogisticsNetwork
import com.itszuvalex.femtocraft.power.tile.ITilePower
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 2/25/2016.
  */
trait ITileAssemblyArray extends ITilePower {

  /**
    *
    * @return Base Power usage * getPowerMultipler = actual power requirement.  Assemblies are responsible for calculating their power usage before draining power.
    */
  def getPowerMultiplier: Double

  /**
    *
    * @return Base Time usage * getTimeMultiplier = actual time requirement.  Assemblies are responsible for calculating the time usage themselves.
    */
  def getTimeMultiplier: Double

  /**
    *
    * @return Set of support Assembly types
    */
  def getSupportedAssemblyTypes: Set[String]

  /**
    *
    * @return IItemLogisticsNetwork connection, or null if no logistics supported.
    */
  def getItemLogisticsNetwork: IItemLogisticsNetwork

  /**
    * Number of assembly slots
    */
  def getAssemblySlots: Int

  /**
    *
    * @param slot (0 until getAssemblySlots)
    * @return IItemAssembly in the given slot.
    */
  def getAssembly(slot: Int): ItemStack

  /**
    *
    * @param assembly Assembly to insert into slot.  Should not be null.
    * @param slot     (0 until getAssemblySlots) to insert into.
    * @return True if slot is empty and assembly was valid, accepted, and placed in the slot.
    */
  def addAssembly(assembly: ItemStack, slot: Int): Boolean

  /**
    *
    * @param slot (0 until getAssemblySlots) to remove from.
    * @return The assembly item stack in given and now empty slot, or null if failed to remove assembly.  (somehow?)
    */
  def removeAssembly(slot: Int): ItemStack

  /**
    *
    * @return Number of slots that are accessible for given IItemAssemblies to withdraw from.
    */
  def getInputSlots: Int

  /**
    *
    * @param slot (0 until getInputSlots)
    * @return Itemstack in given input slot.
    */
  def getInputItem(slot: Int): ItemStack

  /**
    *
    * @param item Item to merge into slot.
    * @param slot (0 until getInputSlots)
    * @return Remainder of item after the add or merge.  Should only be non-null if item doesn't match getInputItem(slot), or not enough space.
    */
  def addOrMergeInputItem(item: ItemStack, slot: Int): ItemStack

  /**
    * Attempts to add an item into any input slots, prioritizing merging, then open slots.
    *
    * @param item Item to merge into slot.
    * @return Remaining ItemStack from item
    */
  def addInputItem(item: ItemStack): ItemStack = {
    var ret = item
    //Merge first
    (0 until getInputSlots).filter(getInputItem(_) != null).exists { inputSlot =>
      ret = addOrMergeInputItem(item, inputSlot)
      ret == null
                                                                   }
    //Fill empty
    (0 until getInputSlots).filter(getInputItem(_) == null).exists { inputSlot =>
      ret = addOrMergeInputItem(item, inputSlot)
      ret == null
                                                                   }
    ret
  }

  /**
    *
    * @param slot (0 until getInputSlots)
    * @param amt  Amount of the item from said slot to remove.
    * @return The itemstack consisting of getInputItem(slot) and of stack size Math.min(getInputItem(slot).stackSize, amt), or null if no item in slot.
    */
  def removeInputItem(slot: Int, amt: Int): ItemStack

  /**
    *
    * @return Number of slots that are accessible for given IItemAssemblies to output to.
    */
  def getOutputSlots: Int

  /**
    *
    * @param slot (0 until getOutputSlots)
    * @return Itemstack in given slot.
    */
  def getOutputItem(slot: Int): ItemStack

  /**
    *
    * @param item Item to merge into slot.
    * @param slot (0 until getOutputSlots)
    * @return Remainder of item after the add or merge.  Should only be non-null if item doesn't match getOutputItem(slot), or not enough space.
    */
  def addOrMergeOutputItem(item: ItemStack, slot: Int): ItemStack

  /**
    * Attempts to add an item into any output slots, prioritizing merging, then open slots.
    *
    * @param item Item to merge into slot.
    * @return Remaining ItemStack from item
    */
  def addOutputItem(item: ItemStack): ItemStack = {
    var ret = item
    //Merge first
    (0 until getOutputSlots).filter(getOutputItem(_) != null).exists { outputSlot =>
      ret = addOrMergeOutputItem(item, outputSlot)
      ret == null
                                                                     }
    //Fill empty
    (0 until getOutputSlots).filter(getOutputItem(_) == null).exists { outputSlot =>
      ret = addOrMergeOutputItem(item, outputSlot)
      ret == null
                                                                     }
    ret
  }

  /**
    *
    * @param slot (0 until getOutputSlots)
    * @param amt  Amount of item from said slot to remove.
    * @return The itemstack consisting of getOutputItem(slot) and of stack size Math.min(getOutputItem(slot).stackSize, amt), or null if no item in slot.
    */
  def removeOutputItem(slot: Int, amt: Int): ItemStack

}

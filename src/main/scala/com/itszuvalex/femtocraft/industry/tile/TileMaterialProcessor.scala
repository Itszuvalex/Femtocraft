package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.industry.item.IItemAssembly
import com.itszuvalex.femtocraft.industry.tile.TileMaterialProcessor._
import com.itszuvalex.femtocraft.logistics.IItemLogisticsNetwork
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory, TileMultiblockIndexedInventoryWithIInventory}
import com.itszuvalex.femtocraft.nanite.INaniteStrain
import com.itszuvalex.femtocraft.power.item.IPowerStorage
import com.itszuvalex.femtocraft.power.node.{IPowerNode, PowerNode}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.Configurable
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import com.itszuvalex.itszulib.util.Comparators.ItemStack._
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.AxisAlignedBB

object TileMaterialProcessor {
  val acceptedAssemblyTypes = Set("Furnace", "Crusher")
  val numInputSlots         = 4
  val numOutputSlots        = 4
  val numAssemblySlots      = 2

  val powerMax = 100000

  private val indexInputStart    = 0
  private val indexAssemblyStart = numInputSlots
  private val indexOutputStart   = numAssemblySlots + numInputSlots
  private val indexPowerSlot     = numInputSlots + numAssemblySlots + numOutputSlots
  private val indexNaniteSlot    = numInputSlots + numAssemblySlots + numOutputSlots + 1
}

@Configurable class TileMaterialProcessor extends TileEntityBase
                                                  with TileMultiblockIndexedInventory
                                                  with TileMultiblockIndexedInventoryWithIInventory
                                                  with PowerNode
                                                  with MultiBlockComponent
                                                  with ITileAssemblyArray {
  this.powerMax = TileMaterialProcessor.powerMax

  override def hasDescription: Boolean = true

  override def defaultInventory: IndexedInventory = new IndexedInventory(numInputSlots + numOutputSlots + numAssemblySlots + 2)


  override def serverUpdate(): Unit = {
    setPower(getMaximumPower)

    (0 until getAssemblySlots).flatMap(i => Option(getAssembly(i))).foreach { item =>
      item.getItem match {
        case assembly: IItemAssembly =>
          assembly.onTick(item, this)
        case _ =>
      }
                                                                            }
  }

  override def getMaximumPower: Double = getPowerMax

  /**
    *
    * @param slot (0 until getAssemblySlots)
    * @return IItemAssembly in the given slot.
    */
  override def getAssembly(slot: Int): ItemStack = {
    if (slot < 0 || slot >= getAssemblySlots) throw new IllegalArgumentException()

    indInventory.getStackInSlot(indexAssemblyStart + slot)
  }

  /**
    * Number of assembly slots
    */
  override def getAssemblySlots = numAssemblySlots

  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: Int): Boolean = {
    if (hasGUI) {
      par5EntityPlayer.openGui(getMod, getGuiID, worldObj, info.x, info.y, info.z)
      return true
    }
    false
  }

  override def getMod: AnyRef = Femtocraft

  override def hasGUI = isValidMultiBlock

  override def getGuiID = GuiIDs.TileMaterialProcessorGuiID

  /**
    *
    * @return The type of PowerNode this is.
    */
  override def getType: String = IPowerNode.DIFFUSION_TARGET_NODE

  override def getRenderBoundingBox: AxisAlignedBB = {
    if (isController) {
      AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 2, yCoord + 3, zCoord + 2)
    }
    else super.getRenderBoundingBox
  }

  /**
    *
    * @return Set of support Assembly types
    */
  override def getSupportedAssemblyTypes = acceptedAssemblyTypes

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
        indInventory.setInventorySlotContents(indexOutputStart + slot, null)
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
      indInventory.setInventorySlotContents(indexInputStart + slot, slotItem)
      null
    }
    else {
      if (IDDamageWildCardNBTComparator.compare(item, slotItem) == 0) {
        val room = slotItem.getMaxStackSize - slotItem.stackSize
        val amount = Math.min(room, item.stackSize)
        slotItem.stackSize += amount
        if (amount <= room) {
          null
        }
        else {
          item.stackSize -= amount
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
    * @param slot (0 until getInputSlots)
    * @return Itemstack in given input slot.
    */
  override def getInputItem(slot: Int): ItemStack = {
    if (slot < 0 || slot >= getInputSlots) throw new IllegalArgumentException()

    indInventory.getStackInSlot(indexInputStart + slot)
  }

  /**
    *
    * @return Number of slots that are accessible for given IItemAssemblies to withdraw from.
    */
  override def getInputSlots = numInputSlots

  /**
    *
    * @param item Item to merge into slot.
    * @param slot (0 until getOutputSlots)
    * @return Remainder of item after the add or merge.  Should only be non-null if item doesn't match getOutputItem(slot), or not enough space.
    */
  override def addOrMergeOutputItem(item: ItemStack, slot: Int): ItemStack = {
    if (item == null) return null

    val slotItem = getOutputItem(slot)
    if (slotItem == null) {
      indInventory.setInventorySlotContents(indexOutputStart + slot, item)
      null
    }
    else {
      if (IDDamageWildCardNBTComparator.compare(item, slotItem) == 0) {
        val room = slotItem.getMaxStackSize - slotItem.stackSize
        val amount = Math.min(room, item.stackSize)
        slotItem.stackSize += amount
        if (amount <= room) {
          null
        }
        else {
          item.stackSize -= amount
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
    * @param slot (0 until getOutputSlots)
    * @return Itemstack in given slot.
    */
  override def getOutputItem(slot: Int): ItemStack = {
    if (slot < 0 || slot >= getOutputSlots) throw new IllegalArgumentException()

    indInventory.getStackInSlot(indexOutputStart + slot)
  }

  /**
    *
    * @return Number of slots that are accessible for given IItemAssemblies to output to.
    */
  override def getOutputSlots = numOutputSlots

  /**
    *
    * @param slot (0 until getAssemblySlots) to remove from.
    * @return The assembly item stack in given and now empty slot, or null if failed to remove assembly.  (somehow?)
    */
  override def removeAssembly(slot: Int): ItemStack = {
    val assembly = getAssembly(slot)
    indInventory.setInventorySlotContents(indexAssemblyStart + slot, null)
    assembly
  }

  /**
    *
    * @param slot (0 until getInputSlots)
    * @param amt  Amount of the item from said slot to remove.
    * @return The itemstack consisting of getInputItem(slot) and of stack size Math.min(getInputItem(slot).stackSize, amt), or null if no item in slot.
    */
  override def removeInputItem(slot: Int, amt: Int): ItemStack = {
    val item = getInputItem(slot)
    if (item != null) {
      val remove = Math.min(item.stackSize, amt)
      item.stackSize -= remove
      if (item.stackSize == 0) {
        indInventory.setInventorySlotContents(indexInputStart + slot, null)
      }
      val ret = item.copy()
      ret.stackSize = remove
      ret
    }
    else null
  }

  /**
    *
    * @param assembly Assembly to insert into slot.  Should not be null.
    * @param slot     (0 until getAssemblySlots) to insert into.
    * @return True if slot is empty and assembly was valid, accepted, and placed in the slot.
    */
  override def addAssembly(assembly: ItemStack, slot: Int): Boolean = {
    val current = getAssembly(slot)
    if (current == null) {
      indInventory.setInventorySlotContents(indexAssemblyStart + slot, assembly)
      true
    }
    else {
      false
    }
  }

  /**
    *
    * @return IItemLogisticsNetwork connection, or null if no logistics supported.
    */
  override def getItemLogisticsNetwork: IItemLogisticsNetwork = null

  /**
    *
    * @param amt      Amount to attempt to charge
    * @param doCharge False to simulate, true to actually do
    * @return Amount of amt used to actually charge.
    */
  override def charge(amt: Double, doCharge: Boolean): Double = addPower(amt, doCharge)

  /**
    *
    * @param amt     Amount of power to drain
    * @param doDrain False to simulate, true to actually remove power.
    * @return Amount of amt that was successfully drained.
    */
  override def drain(amt: Double, doDrain: Boolean): Double = usePower(amt, doDrain)

  override def getCurrentPower: Double = getPowerCurrent

  /**
    *
    * @return Base Power usage * getPowerMultipler = actual power requirement.  Assemblies are responsible for calculating their power usage before draining power.
    */
  override def getPowerMultiplier: Double = 1

  /**
    *
    * @return Base Time usage * getTimeMultiplier = actual time requirement.  Assemblies are responsible for calculating the time usage themselves.
    */
  override def getTimeMultiplier: Double = 1

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean = if (isController) {
    if (item == null) return true

    slot match {
      case input if slot >= indexInputStart && slot < indexInputStart + numInputSlots => true
      case assembly if slot >= indexAssemblyStart && slot < indexOutputStart && item.getItem != null =>
        item.getItem match {
          case assembly: IItemAssembly =>
            getSupportedAssemblyTypes.contains(assembly.getType(item))
          case _ => false
        }
      case output if slot >= indexOutputStart && slot < indexOutputStart + numOutputSlots => true
      case power if slot == indexPowerSlot && item.getItem != null =>
        item.getItem match {
          case assembly: IPowerStorage => true
          case _ => false
        }
      case nanite if slot == indexNaniteSlot && item.getItem != null =>
        item.getItem match {
          case nanite: INaniteStrain => true
          case _ => false
        }
      case _ => false
    }
  }
  else forwardToController[TileMaterialProcessor, Boolean](_.isItemValidForSlot(slot, item))


}

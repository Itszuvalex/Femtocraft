package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.industry.item.{IItemAssembly, ItemFurnaceAssembly, ItemGrinderAssembly}
import com.itszuvalex.femtocraft.industry.tile.TileMaterialProcessor._
import com.itszuvalex.femtocraft.logistics.IItemLogisticsNetwork
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory, TileMultiblockIndexedInventoryWithIInventory}
import com.itszuvalex.femtocraft.nanite.INaniteStrain
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.power.item.{IPowerCrystal, IPowerStorage}
import com.itszuvalex.femtocraft.power.node.{DiffusionTargetNode, IPowerNode, PowerNode}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.Configurable
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import com.itszuvalex.itszulib.util.Comparators.ItemStack._
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.World

object TileMaterialProcessor {
  val acceptedAssemblyTypes = Set(ItemFurnaceAssembly.AssemblyType, ItemGrinderAssembly.AssemblyType)
  val numInputSlots         = 4
  val numOutputSlots        = 4
  val numAssemblySlots      = 2

  val powerMax = 100000

  private val indexInputStart    = 0
  private val indexAssemblyStart = numInputSlots
  private val indexOutputStart   = numAssemblySlots + numInputSlots
  private val indexPowerSlot     = numInputSlots + numAssemblySlots + numOutputSlots
  private val indexNaniteSlot    = numInputSlots + numAssemblySlots + numOutputSlots + 1

  private val INV_COMPOUND_TAG = "Inventory"
}

@Configurable class TileMaterialProcessor extends TileEntityBase
                                                  with TileMultiblockIndexedInventory
                                                  with TileMultiblockIndexedInventoryWithIInventory
                                                  with PowerNode
                                                  with MultiBlockComponent
                                                  with ITileAssemblyArray {
  override def hasDescription: Boolean = true

  override def defaultInventory: IndexedInventory = new IndexedInventory(numInputSlots + numOutputSlots + numAssemblySlots + 2)

  override def onBlockBreak() = {
    if (isController) {
      super.onBlockBreak()
    }
  }

  override def serverUpdate(): Unit = {
    (0 until getAssemblySlots).view.flatMap(i => Option(getAssembly(i))).foreach { item =>
      item.getItem match {
        case assembly: IItemAssembly =>
          assembly.onTick(item, this)
        case _ =>
      }
                                                                                 }
    indInventory.getStackInSlot(indexPowerSlot) match {
      case null =>
      case stack =>
        stack.getItem match {
          case null =>
          case crystal: IPowerCrystal =>
            crystal.onTick(stack)
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
    * @param child
    * @return True if child is capable of being a child of this node.
    */
  override def canAddChild(child: IPowerNode): Boolean = DiffusionTargetNode.canAddChild(child)

  /**
    *
    * @param parent IPowerNode that is being checked.
    * @return True if this node is capable of having that node as a parent.
    */
  override def canSetParent(parent: IPowerNode): Boolean = super.canSetParent(parent) && DiffusionTargetNode.canAddParent(parent)

  /**
    *
    * @param child
    * @return True if child is successfully added.
    */
  override def addChild(child: IPowerNode): Boolean = false

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
    if (item.stackSize == 0) return null

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
    * @param amount Amount of power to add.
    * @param doFill True if actually change values, false to simulate.
    * @return Amount of power used out of @amount to fill the internal storage of this Tile.
    */
  override def addPower(amount: Double, doFill: Boolean): Double = {
    getStackInSlot(indexPowerSlot) match {
      case null =>
        getParentLoc match {
          case null => 0
          case loc =>
            loc.getTileEntity() match {
              case None => 0
              case Some(power: IPowerNode) =>
                power.addPower(amount, doFill)
            }
        }
      case item =>
        item.getItem match {
          case null => 0
          case power: IPowerStorage =>
            power.store(item, amount, doFill)
        }
    }
  }

  /**
    *
    * @param amount Amount of power to consume.
    * @param doUse  True if actually change values, false to simulate.
    * @return Amount of power consumed out of @amount from the internal storage of this Tile.
    */
  override def usePower(amount: Double, doUse: Boolean): Double = {
    getStackInSlot(indexPowerSlot) match {
      case null =>
        getParentLoc match {
          case null => 0
          case loc =>
            loc.getTileEntity() match {
              case None => 0
              case Some(power: IPowerNode) =>
                power.usePower(amount, doUse)
            }
        }
      case item =>
        item.getItem match {
          case null => 0
          case power: IPowerStorage =>
            power.consume(item, amount, doUse)
        }
    }
  }

  /**
    *
    * @return Amount of power currently stored in this node.
    */
  override def getPowerCurrent: Double = {
    getStackInSlot(indexPowerSlot) match {
      case null =>
        getParentLoc match {
          case null => 0
          case loc =>
            loc.getTileEntity() match {
              case None => 0
              case Some(power: IPowerNode) =>
                power.getPowerCurrent
            }
        }
      case item =>
        item.getItem match {
          case null => 0
          case power: IPowerStorage =>
            power.getStorageCurrent(item)
        }
    }
  }

  /**
    *
    * @return Amount of power capable of being stored in this node.
    */
  override def getPowerMax: Double = {
    getStackInSlot(indexPowerSlot) match {
      case null =>
        getParentLoc match {
          case null => 0
          case loc =>
            loc.getTileEntity() match {
              case None => 0
              case Some(power: IPowerNode) =>
                power.getPowerMax
            }
        }
      case item =>
        item.getItem match {
          case null => 0
          case power: IPowerStorage =>
            power.getStorageMax(item)
        }
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
      case input if input >= indexInputStart && input < indexInputStart + numInputSlots => true
      case assembly if assembly >= indexAssemblyStart && assembly < indexOutputStart && item.getItem != null =>
        item.getItem match {
          case assembly: IItemAssembly =>
            getSupportedAssemblyTypes.contains(assembly.getType(item))
          case _ => false
        }
      case output if output >= indexOutputStart && output < indexOutputStart + numOutputSlots => true
      case power if power == indexPowerSlot && item.getItem != null =>
        item.getItem match {
          case storage: IPowerStorage => true
          case _ => false
        }
      case nanite if nanite == indexNaniteSlot && item.getItem != null =>
        item.getItem match {
          case nanite: INaniteStrain => true
          case _ => false
        }
      case _ => false
    }
  }
  else forwardToController[TileMaterialProcessor, Boolean](_.isItemValidForSlot(slot, item))

  override def saveInfoToItemNBT(compound: NBTTagCompound): Unit = {
    super.saveInfoToItemNBT(compound)
    val inv = new NBTTagCompound
    indInventory.saveToNBT(inv)
    compound.setTag(INV_COMPOUND_TAG, inv)
  }

  override def loadInfoFromItemNBT(compound: NBTTagCompound): Unit = {
    super.loadInfoFromItemNBT(compound)
    indInventory.loadFromNBT(compound.getCompoundTag(INV_COMPOUND_TAG))
  }

  override def formMultiBlock(world: World, x: Int, y: Int, z: Int): Boolean = {
    val ret = super.formMultiBlock(world, x, y, z)
    if (isController) PowerManager.addNode(this)
    ret
  }

  override def validate(): Unit = {
    super.validate()
    if (!worldObj.isRemote && isController) PowerManager.addNode(this)
  }

}

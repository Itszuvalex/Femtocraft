package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.industry.DustRecipeRegistry
import com.itszuvalex.femtocraft.industry.tile.TileDemolisher.DemolishTask._
import com.itszuvalex.femtocraft.industry.tile.TileDemolisher.{DemolishTask, TASK_NBT}
import com.itszuvalex.femtocraft.power.node.PowerLeafNode
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.Capabilities
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.{Converter, IBattery, IItemStack, PowerBattery}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileInventory
import com.itszuvalex.itszulib.util.Task
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

object TileDemolisher {
  val TICKS_REQ      = 20 * 8
  val POWER_PER_TICK = 10
  val POWER_REQ      = TICKS_REQ * POWER_PER_TICK

  val TASK_NBT = "Task"

  object DemolishTask {
    val DEMOLISHING_STACK_NBT   = "Demolish"
    val DEMOLISHING_SMELTED_NBT = "Demolished"
  }

  class DemolishTask(var stack: IItemStack) extends Task(POWER_REQ, TICKS_REQ) {
    var demolished = false

    override def deserializeNBT(t: NBTTagCompound): Unit = {
      super.deserializeNBT(t)
      if (t.hasKey(DEMOLISHING_STACK_NBT))
        stack = IItemStack.createFromNBT(t.getCompoundTag(DEMOLISHING_STACK_NBT))
      demolished = t.getBoolean(DEMOLISHING_SMELTED_NBT)
    }

    override def serializeNBT(): NBTTagCompound = {
      val ret = super.serializeNBT()
      if (stack != null)
        ret.setTag(DEMOLISHING_STACK_NBT, stack.serializeNBT())
      ret.setBoolean(DEMOLISHING_SMELTED_NBT, demolished)
      ret
    }

    override def reset(): Unit = {
      super.reset()
      stack = IItemStack.Empty
      demolished = false
    }
  }

}

class TileDemolisher extends TileEntityBase with TileInventory with PowerLeafNode {
  private val task: DemolishTask = new TileDemolisher.DemolishTask(IItemStack.Empty)

  override def connectionRadius: Float = 8f

  override def leafTransferRate = 50d

  override def defaultBattery: IBattery = new PowerBattery(5000)

  override def storageType: PowerStorageNodeType = PowerStorageNodeType.CONSUMER

  override def defaultStorage: IItemStorage = new ItemStorageArray(2) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = {
      isItemValidForSlot(i, stack.toMinecraft)
    }
  }

  override def getFieldCount: Int = 0

  override def getField(id: Int): Int = 0

  override def setField(id: Int, value: Int): Unit = {}

  override def getMod: AnyRef = Femtocraft

  override def hasDescription: Boolean = true

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean = {
    if (slot == 0) {
      val result = DustRecipeRegistry.getDust(item).getOrElse(ItemStack.EMPTY)
      result != null && !result.isEmpty
    }
    else false
  }

  override def hasGUI = true

  override def getGuiID = GuiIDs.TileDemolisherGuiID

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    if (task.stack == null || task.stack.isEmpty) {
      val item = storage(0)
      if (!item.isEmpty && DustRecipeRegistry.getDust(item.toMinecraft).isDefined) {
        val ins = storage.split(0, 1)
        task.reset()
        task.stack = ins
      }
    }
    else {
      battery.storage -= task.contribute(Math.min(task.powerPerTick(0, 0), battery.storage), 0, 0)
      if (task.completed(0)) {
        val item = task.stack
        if (item == null || item.isEmpty) {
          task.reset()
          return
        }

        var insertItem = task.stack
        if (!task.demolished) {
          val resultItem = DustRecipeRegistry.getDust(item.toMinecraft).getOrElse(ItemStack.EMPTY)
          if (resultItem == null || resultItem.isEmpty) {
            task.reset()
            return
          }
          else {
            insertItem = Converter.IItemStackFromItemStack(resultItem.copy())
          }

          task.demolished = true
        }

        // Will clear the stack once we successfully insert the result item or set stack to the finished result
        task.stack = storage.insert(1, insertItem)
        if (task.stack == null || task.stack.isEmpty)
          task.reset()
      }
    }
  }

  def setProgress(progress: Double): Unit = {
    task.progress = progress
  }

  def getProgress = task.progress

  def getProgressMax = task.adjustedMax(0)

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    super.deserializeNBT(nbt)
    task.deserializeNBT(nbt.getCompoundTag(TASK_NBT))
  }

  override def serializeNBT(): NBTTagCompound = {
    val ret = super.serializeNBT()
    ret.setTag(TASK_NBT, task.serializeNBT())
    ret
  }

  //TODO: Remove once Itszulib version bump
  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if (capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) true
    else if (capability == Capabilities.ITEM_STORAGE) true
    else
      super.hasCapability(capability, facing)
  }
}

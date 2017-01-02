package com.itszuvalex.femtocraft.nanite.tile

import com.itszuvalex.femtocraft.api.power.{PowerConnectionNodeType, PowerStorageNodeType}
import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace.TASK_NBT
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteExtractor.ExtractTask
import com.itszuvalex.femtocraft.nanite.{NaniteStack, NaniteTank, TileNaniteStorage}
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.power.node.PowerNode
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.{IBattery, PowerBattery}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileInventory
import com.itszuvalex.itszulib.util.Task
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound


object TileNaniteExtractor {
  val TICKS_REQ      = 20 * 8
  val POWER_PER_TICK = 10
  val POWER_REQ      = TICKS_REQ * POWER_PER_TICK

  val TASK_NBT = "Task"

  object ExtractTask {
    val NANITES_NBT = "Nanite"
  }

  class ExtractTask(var stack: NaniteStack) extends Task(POWER_REQ, TICKS_REQ) {

    override def deserializeNBT(t: NBTTagCompound): Unit = {
      super.deserializeNBT(t)
      stack = NaniteStack.loadFromNBT(t.getCompoundTag(ExtractTask.NANITES_NBT))
    }

    override def serializeNBT(): NBTTagCompound = {
      val ret = super.serializeNBT()
      if (stack != null)
        ret.setTag(ExtractTask.NANITES_NBT, stack.serializeNBT())
      ret
    }

    override def reset(): Unit = {
      super.reset()
      stack = null
    }
  }

}

class TileNaniteExtractor extends TileEntityBase with TileInventory with PowerNode with TileNaniteStorage {
  private val task: ExtractTask = new ExtractTask(null)

  override def defaultBattery: IBattery = new PowerBattery(5000)

  override def powerStorageType: PowerStorageNodeType = PowerStorageNodeType.CONSUMER

  override def powerConnectionType: PowerConnectionNodeType = PowerConnectionNodeType.LEAF

  override def powerRadius: Float = 8f

  override def powerTransfer: Double = 50d

  override def rendersPower: Boolean = false

  override def defaultStorageTank: NaniteTank = new NaniteTank(50)

  override def defaultStorage: IItemStorage = new ItemStorageArray(1)

  override def getFieldCount: Int = 0

  override def getField(id: Int): Int = 0

  override def setField(id: Int, value: Int): Unit = {}

  override def getMod: AnyRef = Femtocraft

  override def hasDescription: Boolean = true

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean = {
    CybermaterialRegistry.getNaniteFromItem(item.getItem, item.getItemDamage).isDefined
  }

  override def hasGUI = true

  override def getGuiID = GuiIDs.TileNaniteExtractorID

  override def serverUpdate(): Unit = {
    if (task.stack == null) {
      val item = storage(0)
      if (!item.isEmpty) {
        val ins = storage.split(0, 1)
        task.reset()
        task.stack = CybermaterialRegistry.getNaniteFromItem(ins.item, ins.damage).map(_.copy()).orNull
      }
    }
    else {
      powerDelegate.storage.storage -= task.contribute(Math.min(task.powerPerTick(0, 0), powerDelegate.storage.storage), 0, 0)
      if (task.completed(0)) {
        val stack = task.stack
        if (stack == null || stack.volume <= 0) {
          task.reset()
          return
        }

        task.stack = storageTank.fill(stack, true)
        if (task.stack == null) {
          task.reset()
        }
      }
    }
  }

  def setProgress(progress: Double): Unit = {
    task.progress = progress
  }

  def getProgress = task.progress

  def getProgressMax = task.adjustedMax(0)

  /* Tile Entity */
  override def validate(): Unit = {
    super.validate()
    if (!getWorld.isRemote) PowerManager.addNode(powerDelegate)
  }

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    super.deserializeNBT(nbt)
    task.deserializeNBT(nbt.getCompoundTag(TASK_NBT))
  }

  override def serializeNBT(): NBTTagCompound = {
    val ret = super.serializeNBT()
    ret.setTag(TASK_NBT, task.serializeNBT())
    ret
  }

  override def func_191420_l(): Boolean = true
}

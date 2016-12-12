package com.itszuvalex.femtocraft.nanite.tile

import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace.TASK_NBT
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteExtractor.ExtractTask
import com.itszuvalex.femtocraft.nanite.{NaniteStack, NaniteTank, TileNaniteStorage}
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.power.node.{IPowerNode, PowerNode}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
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
  powerMax = 4000

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

  /**
    *
    * @return The type of PowerNode this is.
    */
  override def getType: String = IPowerNode.DIFFUSION_TARGET_NODE

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
      usePower(task.contribute(Math.min(task.powerPerTick(0, 0), getPowerCurrent), 0, 0), doUse = true)
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
    if (!getWorld.isRemote) PowerManager.addNode(this)
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

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    savePowerConnectionInfo(compound)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    loadPowerConnectionInfo(compound)
  }

  /**
    *
    * @param parent Parent being set.
    *
    * @return True if parent is successfully set to input parent.
    */
  override def setParent(parent: IPowerNode): Boolean = {
    val ret = super.setParent(parent)
    setUpdate()
    ret
  }

  /**
    *
    * @param child
    *
    * @return True if child is capable of being a child of this node.
    */
  override def canAddChild(child: IPowerNode): Boolean = false

  /**
    *
    * @param parent IPowerNode that is being checked.
    *
    * @return True if this node is capable of having that node as a parent.
    */
  override def canSetParent(parent: IPowerNode): Boolean = super.canSetParent(parent) && parent.getType == IPowerNode.CRYSTAL_MOUNT

  override def func_191420_l(): Boolean = true
}

package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.industry.DustRecipeRegistry
import com.itszuvalex.femtocraft.industry.tile.CrystalCrusherModule.CrushTask
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.storage.{IBattery, IItemStorage}
import com.itszuvalex.itszulib.api.wrappers.{IItemStack, ITileEntity}
import com.itszuvalex.itszulib.core.TileEntityInternalModuleTickable
import com.itszuvalex.itszulib.util.Task
import net.minecraft.nbt.NBTTagCompound

object CrystalCrusherModule {
  val TICKS_REQ      = 20 * 8
  val POWER_PER_TICK = 10
  val POWER_REQ      = TICKS_REQ * POWER_PER_TICK

  val TASK_NBT = "Task"

  class CrushTask(var stack: IItemStack) extends Task(POWER_REQ, TICKS_REQ) {
    import com.itszuvalex.femtocraft.industry.tile.CrystalCrusherModule.CrushTask._

    var crushed = false

    override def deserializeNBT(t: NBTTagCompound): Unit = {
      super.deserializeNBT(t)
      if (t.hasKey(CRUSHING_STACK_NBT))
        stack = IItemStack.createFromNBT(t.getCompoundTag(CRUSHING_STACK_NBT))
      crushed = t.getBoolean(CRUSHING_SMELTED_NBT)
    }

    override def serializeNBT(): NBTTagCompound = {
      val ret = super.serializeNBT()
      if (stack != null)
        ret.setTag(CRUSHING_STACK_NBT, stack.serializeNBT())
      ret.setBoolean(CRUSHING_SMELTED_NBT, crushed)
      ret
    }

    override def reset(): Unit = {
      super.reset()
      stack = IItemStack.Empty
      crushed = false
    }
  }

  object CrushTask {
    val CRUSHING_STACK_NBT   = "Crush"
    val CRUSHING_SMELTED_NBT = "Crushed"
  }
}

class CrystalCrusherModule(val input: IItemStorage, val output: IItemStorage, val batteryStorage: IItemStorage, val battery: IBattery) extends TileEntityInternalModuleTickable[CrystalCrusherModule] {
  private val task: CrushTask = new CrushTask(IItemStack.Empty)

  override def module: IModule[CrystalCrusherModule] = TileCrystalCrusher.MODULE

  override def serverUpdate(tile: ITileEntity): Unit = {
    batteryStorage.head.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null).foreach(_.onTick())

    if (task.stack == null || task.stack.isEmpty) {
      val item = input.head
      if (!item.isEmpty && DustRecipeRegistry.getDust(item).isDefined) {
        val ins = input.split(0, 1)
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
        if (!task.crushed) {
          val resultItem = DustRecipeRegistry.getDust(item).getOrElse(IItemStack.Empty)
          if (resultItem == null || resultItem.isEmpty) {
            task.reset()
            return
          }
          else {
            insertItem = resultItem.copy()
          }

          task.crushed = true
        }

        // Will clear the stack once we successfully insert the result item or set stack to the finished result
        task.stack = output.insert(0, insertItem)
        if (task.stack == null || task.stack.isEmpty)
          task.reset()
      }
    }
  }

  def getProgress: Double = task.progress

  def setProgress(progress: Double): Unit = {
    task.progress = progress
  }

  def getProgressMax: Double = task.adjustedMax(0)

  override def hasWorldNBT: Boolean = true

  override def writeWorldNBT(tag: NBTTagCompound): Unit = {
    tag.setTag(DemolisherModule.TASK_NBT, task.serializeNBT())
  }

  override def readWorldNBT(tagCompound: NBTTagCompound): Unit = {
    task.deserializeNBT(tagCompound.getCompoundTag(DemolisherModule.TASK_NBT))
  }
}

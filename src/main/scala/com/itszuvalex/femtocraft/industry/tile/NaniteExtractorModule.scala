package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.nanite.{INaniteTank, NaniteStack}
import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.industry.tile.NaniteExtractorModule.ExtractTask
import com.itszuvalex.femtocraft.temp.TileEntityInternalModuleTickable
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.storage.{IBattery, IItemStorage}
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.util.Task
import net.minecraft.nbt.NBTTagCompound

object NaniteExtractorModule {
  val TICKS_REQ      = 20 * 8
  val POWER_PER_TICK = 10
  val POWER_REQ      = TICKS_REQ * POWER_PER_TICK
  val TASK_NBT       = "Task"

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

  object ExtractTask {
    val NANITES_NBT = "Nanite"
  }

}

class NaniteExtractorModule(val input: IItemStorage, val battery: IBattery, val tank: INaniteTank) extends TileEntityInternalModuleTickable[NaniteExtractorModule] {
  private val task: ExtractTask = new ExtractTask(null)

  override def module: IModule[NaniteExtractorModule] = TileNaniteExtractor.MODULE

  override def hasWorldNBT: Boolean = true

  override def writeWorldNBT(tag: NBTTagCompound): Unit = {
    tag.setTag(NanoFurnaceModule.TASK_NBT, task.serializeNBT())
  }

  override def readWorldNBT(tagCompound: NBTTagCompound): Unit = {
    task.deserializeNBT(tagCompound.getCompoundTag(NanoFurnaceModule.TASK_NBT))
  }


  override def serverUpdate(tile: ITileEntity): Unit = {
    if (task.stack == null) {
      val item = input.head
      if (!item.isEmpty) {
        val ins = input.split(0, 1)
        task.reset()
        task.stack = CybermaterialRegistry.getNaniteFromItem(ins.item, ins.damage).map(_.copy()).orNull
      }
    }
    else {
      battery.storage -= task.contribute(Math.min(task.powerPerTick(0, 0), battery.storage), 0, 0)
      if (task.completed(0)) {
        val stack = task.stack
        if (stack == null || stack.volume <= 0) {
          task.reset()
          return
        }

        task.stack = tank.fill(stack, true)
        if (task.stack == null) {
          task.reset()
        }
      }
    }
  }

  def getProgress: Double = task.progress

  def setProgress(progress: Double): Unit = {
    task.progress = progress
  }

  def getProgressMax: Double = task.adjustedMax(0)
}

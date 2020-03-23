package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.industry.tile.NanoFurnaceModule.SmeltTask
import com.itszuvalex.femtocraft.industry.tile.NanoFurnaceModule.SmeltTask._
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.storage.{IBattery, IItemStorage}
import com.itszuvalex.itszulib.api.wrappers.{Converter, IItemStack, ITileEntity}
import com.itszuvalex.itszulib.core.TileEntityModuleTickable
import com.itszuvalex.itszulib.util.Task
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

object NanoFurnaceModule {
  val TASK_NBT = "Task"

  val TICKS_REQ      = 20 * 8
  val POWER_PER_TICK = 10
  val POWER_REQ      = TICKS_REQ * POWER_PER_TICK

  class SmeltTask(var stack: IItemStack) extends Task(POWER_REQ, TICKS_REQ) {
    var smelted = false

    override def deserializeNBT(t: NBTTagCompound): Unit = {
      super.deserializeNBT(t)
      if (t.hasKey(SMELTING_STACK_NBT))
        stack = IItemStack.createFromNBT(t.getCompoundTag(SMELTING_STACK_NBT))
      smelted = t.getBoolean(SMELTING_SMELTED_NBT)
    }

    override def serializeNBT(): NBTTagCompound = {
      val ret = super.serializeNBT()
      if (stack != null)
        ret.setTag(SMELTING_STACK_NBT, stack.serializeNBT())
      ret.setBoolean(SMELTING_SMELTED_NBT, smelted)
      ret
    }

    override def reset(): Unit = {
      super.reset()
      stack = IItemStack.Empty
      smelted = false
    }
  }

  object SmeltTask {
    val SMELTING_STACK_NBT   = "Smelt"
    val SMELTING_SMELTED_NBT = "Smelted"
  }

}

class NanoFurnaceModule(val input: IItemStorage, val output: IItemStorage, var battery: IBattery) extends TileEntityModuleTickable[NanoFurnaceModule] {
  private val task: SmeltTask = new SmeltTask(IItemStack.Empty)

  override def module: IModule[NanoFurnaceModule] = TileNanoFurnace.MODULE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[NanoFurnaceModule] = _ => Some(this)

  override def hasWorldNBT: Boolean = true

  override def writeWorldNBT(tag: NBTTagCompound): Unit = {
    tag.setTag(NanoFurnaceModule.TASK_NBT, task.serializeNBT())
  }

  override def readWorldNBT(tagCompound: NBTTagCompound): Unit = {
    task.deserializeNBT(tagCompound.getCompoundTag(NanoFurnaceModule.TASK_NBT))
  }

  override def serverUpdate(tile: ITileEntity): Unit = {
    if (task.stack == null || task.stack.isEmpty) {
      val item = input.head
      if (!item.isEmpty && !FurnaceRecipes.instance().getSmeltingResult(item.toMinecraft).isEmpty) {
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
        if (!task.smelted) {
          val resultItem = FurnaceRecipes.instance().getSmeltingResult(item.toMinecraft)
          if (resultItem == null || resultItem.isEmpty) {
            task.reset()
            return
          }
          else {
            insertItem = Converter.IItemStackFromItemStack(resultItem.copy())
          }

          task.smelted = true
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

}

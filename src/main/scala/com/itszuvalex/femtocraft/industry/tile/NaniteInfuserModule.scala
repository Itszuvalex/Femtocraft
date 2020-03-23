package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.nanite.INaniteTank
import com.itszuvalex.femtocraft.industry.NaniteInfusionRecipeRegistry
import com.itszuvalex.femtocraft.industry.tile.NaniteInfuserModule.InfuseTask
import com.itszuvalex.femtocraft.industry.tile.NaniteInfuserModule.InfuseTask._
import com.itszuvalex.femtocraft.temp.TileEntityInternalModuleTickable
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.storage.{IBattery, IItemStorage}
import com.itszuvalex.itszulib.api.wrappers.{IItemStack, ITileEntity}
import com.itszuvalex.itszulib.util.Task
import net.minecraft.nbt.NBTTagCompound

object NaniteInfuserModule {
  val TICKS_REQ      = 20 * 20
  val POWER_PER_TICK = 10
  val POWER_REQ      = TICKS_REQ * POWER_PER_TICK

  class InfuseTask(var stack: IItemStack) extends Task(POWER_REQ, TICKS_REQ) {
    var infused = false

    override def deserializeNBT(t: NBTTagCompound): Unit = {
      super.deserializeNBT(t)
      if (t.hasKey(INFUSING_STACK_NBT))
        stack = IItemStack.createFromNBT(t.getCompoundTag(INFUSING_STACK_NBT))
      infused = t.getBoolean(INFUSED_NBT)
    }

    override def serializeNBT(): NBTTagCompound = {
      val ret = super.serializeNBT()
      if (stack != null)
        ret.setTag(INFUSING_STACK_NBT, stack.serializeNBT())
      ret.setBoolean(INFUSED_NBT, infused)
      ret
    }

    override def reset(): Unit = {
      super.reset()
      stack = IItemStack.Empty
      infused = false
    }
  }

  object InfuseTask {
    val INFUSING_STACK_NBT = "Infuse"
    val INFUSED_NBT        = "Infused"
  }

}

class NaniteInfuserModule(val input: IItemStorage, val output: IItemStorage, val battery: IBattery, val ntank: INaniteTank) extends TileEntityInternalModuleTickable[NaniteInfuserModule] {
  private val task: InfuseTask = new InfuseTask(IItemStack.Empty)

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
      if (!item.isEmpty) {
        val recipe = NaniteInfusionRecipeRegistry.getMatchingRecipe(item)
        if (recipe.isDefined) {
          val r = recipe.get
          if (ntank.containsNanite(r.nanitesRequired.nanite)) {
            val fakeDrain = ntank.drain(r.nanitesRequired.nanite, r.nanitesRequired.volume, false)
            if (fakeDrain != null && fakeDrain.volume == r.nanitesRequired.volume) {
              ntank.drain(r.nanitesRequired.nanite, r.nanitesRequired.volume, true)
              val ins = input.split(0, 1)
              task.reset()
              task.stack = ins
            }
          }
        }
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
        if (!task.infused) {
          val resultItem = NaniteInfusionRecipeRegistry.getMatchingRecipe(insertItem)
          if (resultItem == null || resultItem.isEmpty) {
            task.reset()
            return
          }
          else {
            insertItem = resultItem.get.output.copy()
          }

          task.infused = true
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

  override def module: IModule[NaniteInfuserModule] = TileNaniteInfuser.MODULE
}

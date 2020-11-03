package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.common.BatteryPoweredTask
import com.itszuvalex.femtocraft.industry.LiquifierRecipeRegistry
import com.itszuvalex.femtocraft.industry.tile.CrystalLiquifierModule.LiquifyTask
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.storage.{IBattery, IFluidStorageModifiable, IItemStorage}
import com.itszuvalex.itszulib.api.wrappers.{IFluidStack, IItemStack, ITileEntity}
import com.itszuvalex.itszulib.core.TileEntityInternalModuleTickable
import net.minecraft.nbt.NBTTagCompound

object CrystalLiquifierModule {
  val TICKS_REQ      = 20 * 8
  val POWER_PER_TICK = 10
  val POWER_REQ      = TICKS_REQ * POWER_PER_TICK

  val TASK_NBT = "Task"

  class LiquifyTask(var stack: IItemStack, var fluid: IFluidStack, input: IItemStorage, output: IFluidStorageModifiable, bat: IBattery) extends BatteryPoweredTask(POWER_REQ, TICKS_REQ, bat, () => 0d, () => 0d) {

    import com.itszuvalex.femtocraft.industry.tile.CrystalLiquifierModule.LiquifyTask._

    override def inProgress: Boolean = (stack != null && !stack.isEmpty) || (fluid != null && !fluid.isEmpty && fluid.amount != 0)

    override def canStart: Boolean = {
      val item = input.head
      !item.isEmpty && LiquifierRecipeRegistry.findMatchingRecipe(item).isDefined
    }

    override def start(): Unit = {
      val ins = input.split(0, 1)
      reset()
      stack = ins
    }

    override def onCompleted(): Unit = {
      val item = stack
      if ((item == null || item.isEmpty) && (fluid == null || fluid.isEmpty)) {
        reset()
        return
      }

      if (!liquified) {
        val resultFluidRecipe = LiquifierRecipeRegistry.findMatchingRecipe(item)
        if (resultFluidRecipe == null || resultFluidRecipe.isEmpty || resultFluidRecipe.get.output == null || resultFluidRecipe.get.output.isEmpty) {
          reset()
          return
        }
        else {
          fluid = resultFluidRecipe.get.output.copy()
        }

        liquified = true
      }

      if (fluid == null || fluid.isEmpty || fluid.amount <= 0) {
        reset()
        return
      }

      // Will clear the stack once we successfully insert the result item or set stack to the finished result
      val drained = output.fill(fluid, doFill = true)
      fluid.amount -= drained
      if (fluid.isEmpty || fluid.amount <= 0)
        reset()
    }

    var liquified = false

    override def deserializeNBT(t: NBTTagCompound): Unit = {
      super.deserializeNBT(t)
      if (t.hasKey(LIQUIFYING_STACK_NBT))
        stack = IItemStack.createFromNBT(t.getCompoundTag(LIQUIFYING_STACK_NBT))
      liquified = t.getBoolean(LIQUIFYING_LIQUIFIED_NBT)
      if (t.hasKey(LIQUIFIED_LIQUID_NBT))
        fluid = IFluidStack.createFromNBT(t.getCompoundTag(LIQUIFIED_LIQUID_NBT))
    }

    override def serializeNBT(): NBTTagCompound = {
      val ret = super.serializeNBT()
      if (stack != null && !stack.isEmpty)
        ret.setTag(LIQUIFYING_STACK_NBT, stack.serializeNBT())
      ret.setBoolean(LIQUIFYING_LIQUIFIED_NBT, liquified)
      if (fluid != null && !stack.isEmpty)
        ret.setTag(LIQUIFIED_LIQUID_NBT, fluid.serializeNBT())
      ret
    }

    override def reset(): Unit = {
      super.reset()
      stack = IItemStack.Empty
      fluid = IFluidStack.Empty
      liquified = false
    }
  }

  object LiquifyTask {
    val LIQUIFYING_STACK_NBT     = "Liquifying"
    val LIQUIFYING_LIQUIFIED_NBT = "Liquified"
    val LIQUIFIED_LIQUID_NBT     = "Liquid"
  }

}

class CrystalLiquifierModule(val input: IItemStorage, val output: IFluidStorageModifiable, val batteryStorage: IItemStorage, val battery: IBattery) extends TileEntityInternalModuleTickable[CrystalLiquifierModule] {
  private val task: LiquifyTask = new LiquifyTask(IItemStack.Empty, IFluidStack.Empty, input, output, battery)

  override def module: IModule[CrystalLiquifierModule] = TileCrystalLiquifier.MODULE

  override def serverUpdate(tile: ITileEntity): Unit = {
    batteryStorage.head.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null).foreach(_.onTick())
    task.tick()
  }

  def getProgress: Double = task.progress

  def setProgress(progress: Double): Unit = {
    task.progress = progress
  }

  def getProgressMax: Double = task.adjustedMax(0)

  override def hasWorldNBT: Boolean = true

  override def writeWorldNBT(tag: NBTTagCompound): Unit = {
    tag.setTag(CrystalLiquifierModule.TASK_NBT, task.serializeNBT())
  }

  override def readWorldNBT(tagCompound: NBTTagCompound): Unit = {
    task.deserializeNBT(tagCompound.getCompoundTag(CrystalLiquifierModule.TASK_NBT))
  }
}

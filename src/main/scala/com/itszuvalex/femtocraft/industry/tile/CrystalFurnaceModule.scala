package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.common.BatteryPoweredTask
import com.itszuvalex.femtocraft.industry.tile.CrystalFurnaceModule._
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.storage.{IBattery, IItemStorage}
import com.itszuvalex.itszulib.api.wrappers.{Converter, IItemStack, ITileEntity}
import com.itszuvalex.itszulib.core.TileEntityInternalModuleTickable
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.nbt.NBTTagCompound

object CrystalFurnaceModule {
  val TASK_NBT = "Task"

  val TICKS_REQ      = 20 * 8
  val POWER_PER_TICK = 10
  val POWER_REQ      = TICKS_REQ * POWER_PER_TICK

  class SmeltTask(var stack: IItemStack, input: IItemStorage, output: IItemStorage, battery: IBattery) extends BatteryPoweredTask(POWER_REQ, TICKS_REQ, battery, () => 0d, () => 0d) {

    import com.itszuvalex.femtocraft.industry.tile.CrystalFurnaceModule.SmeltTask._

    var smelted = false

    override def inProgress: Boolean = !(stack == null || stack.isEmpty)

    override def canStart: Boolean = {
      val item = input.head
      !item.isEmpty && !FurnaceRecipes.instance().getSmeltingResult(item.toMinecraft).isEmpty
    }

    override def start(): Unit = {
      val ins = input.split(0, 1)
      reset()
      stack = ins
    }

    override def onCompleted(): Unit = {
      val item = stack
      if (item == null || item.isEmpty) {
        reset()
        return
      }

      var insertItem = stack
      if (!smelted) {
        val resultItem = FurnaceRecipes.instance().getSmeltingResult(item.toMinecraft)
        if (resultItem == null || resultItem.isEmpty) {
          reset()
          return
        }
        else {
          insertItem = Converter.IItemStackFromItemStack(resultItem.copy())
        }

        smelted = true
      }

      // Will clear the stack once we successfully insert the result item or set stack to the finished result
      stack = output.insert(0, insertItem)
      if (stack == null || stack.isEmpty)
        reset()
    }

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

class CrystalFurnaceModule(val input: IItemStorage, val output: IItemStorage, val batteryStorage: IItemStorage, val battery: IBattery) extends TileEntityInternalModuleTickable[CrystalFurnaceModule] {
  private val task: SmeltTask = new SmeltTask(IItemStack.Empty, input, output, battery)

  override def module: IModule[CrystalFurnaceModule] = TileCrystalFurnace.MODULE

  override def hasWorldNBT: Boolean = true

  override def writeWorldNBT(tag: NBTTagCompound): Unit = {
    tag.setTag(TASK_NBT, task.serializeNBT())
  }

  override def readWorldNBT(tagCompound: NBTTagCompound): Unit = {
    task.deserializeNBT(tagCompound.getCompoundTag(TASK_NBT))
  }

  override def serverUpdate(tile: ITileEntity): Unit = {
    batteryStorage.head.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null).foreach(_.onTick())
    task.tick()
  }

  def getProgress: Double = task.progress

  def setProgress(progress: Double): Unit = {
    task.progress = progress
  }

  def getProgressMax: Double = task.adjustedMax(0)

}

package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.industry.DustRecipeRegistry
import com.itszuvalex.femtocraft.industry.tile.TileDemolisher.DemolishTask._
import com.itszuvalex.femtocraft.industry.tile.TileDemolisher._
import com.itszuvalex.femtocraft.power.node.PowerLeafNode
import com.itszuvalex.femtocraft.util.TileEntityUtils
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.Capabilities
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray, ItemStorageSlice}
import com.itszuvalex.itszulib.api.wrappers.{Converter, IBattery, IItemStack, PowerBattery}
import com.itszuvalex.itszulib.core.traits.tile.{BlockFacing, TileInventory}
import com.itszuvalex.itszulib.core.{EnumAutomaticIO, SidedItemStorageConfiguration, TileEntityBase}
import com.itszuvalex.itszulib.util.Task
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler

object TileDemolisher {
  val TICKS_REQ      = 20 * 8
  val POWER_PER_TICK = 10
  val POWER_REQ      = TICKS_REQ * POWER_PER_TICK

  val INPUT_INV_KEY  = "Input"
  val OUTPUT_INV_KEY = "Output"
  val NONE_INV_KEY   = "None"

  val TASK_NBT              = "Task"
  val ITEM_SIDED_CONFIG_NBT = "ItemConfig"
  val TICKS_NBT             = "Ticks"

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
  private val task         : DemolishTask = new DemolishTask(IItemStack.Empty)
  private val inputStorage : IItemStorage = new ItemStorageSlice(storage, Array(0))
  private val outputStorage: IItemStorage = new ItemStorageSlice(storage, Array(1))
  private val sidedStorageConfig          = new SidedItemStorageConfiguration({
    case EnumFacing.UP | EnumFacing.SOUTH => INPUT_INV_KEY
    case EnumFacing.DOWN | EnumFacing.EAST | EnumFacing.WEST | EnumFacing.NORTH => OUTPUT_INV_KEY
    case _ => NONE_INV_KEY
  },
  Map(NONE_INV_KEY -> IItemStorage.Empty,
    INPUT_INV_KEY -> inputStorage,
    OUTPUT_INV_KEY -> outputStorage),
  () => world.getBlockState(pos).getValue(BlockFacing.FACING))
  var ticks = 0

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

    ticks = (ticks - 1 + TileNanoFurnace.TICKS_FOR_AUTOIO) % TileNanoFurnace.TICKS_FOR_AUTOIO
    val isTick = ticks == 0
    // Input
    if (isTick) {
      var inputSize = 1
      TileEntityUtils.getStoragesForIO(this, sidedStorageConfig, EnumAutomaticIO.INPUT).exists { pair =>
        inputSize = pair._1.transferIntoStorage(pair._2, inputSize)
        inputSize <= 0
      }
    }

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

    if (isTick) {
      var outputSize = 1
      TileEntityUtils.getStoragesForIO(this, sidedStorageConfig, EnumAutomaticIO.OUTPUT).exists { pair =>
        outputSize = pair._2.transferIntoStorage(pair._1, outputSize)
        outputSize <= 0
      }
    }
  }

  def setProgress(progress: Double): Unit = {
    task.progress = progress
  }

  def getProgress = task.progress

  def getProgressMax = task.adjustedMax(0)

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = (capability, facing) match {
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.ITEM_STORAGE_CONFIGURABLE => true
    case _ => super.hasCapability(capability, facing)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = (capability, facing) match {
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.ITEM_STORAGE_CONFIGURABLE => sidedStorageConfig.asInstanceOf[T]
    case (_, null) => super.getCapability(capability, facing)
    case (cap, _) if cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => Converter.IItemHandlerModifiableFromIItemStorage(sidedStorageConfig.getStorageForGlobalFacing(facing)).asInstanceOf[T]
    case (cap, _) if cap == Capabilities.ITEM_STORAGE => sidedStorageConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case _ => super.getCapability(capability, facing)
  }

  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)
    task.deserializeNBT(nbt.getCompoundTag(TASK_NBT))
    if (nbt.hasKey(ITEM_SIDED_CONFIG_NBT))
      sidedStorageConfig.deserializeNBT(nbt.getCompoundTag(ITEM_SIDED_CONFIG_NBT))
    ticks = nbt.asInstanceOf[NBTTagCompound].getInteger(TICKS_NBT)
  }

  override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(nbt)
    nbt.setTag(TASK_NBT, task.serializeNBT())
    nbt.setTag(ITEM_SIDED_CONFIG_NBT, sidedStorageConfig.serializeNBT())
    nbt.setInteger(TICKS_NBT, ticks)
    nbt
  }
}

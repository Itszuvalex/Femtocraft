package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.nanite.{NaniteStack, NaniteTank}
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.industry.tile.TileNaniteExtractor._
import com.itszuvalex.femtocraft.nanite.{SidedNaniteStorageConfiguration, TileNaniteStorage}
import com.itszuvalex.femtocraft.power.node.PowerLeafNode
import com.itszuvalex.femtocraft.util.TileEntityUtils
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.Capabilities
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.{Converter, IItemStack, PowerBattery}
import com.itszuvalex.itszulib.core.traits.tile.{BlockFacing, TileInventory}
import com.itszuvalex.itszulib.core.{SidedItemStorageConfiguration, TileEntityBase}
import com.itszuvalex.itszulib.util.Task
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler


object TileNaniteExtractor {
  val TICKS_REQ      = 20 * 8
  val POWER_PER_TICK = 10
  val POWER_REQ      = TICKS_REQ * POWER_PER_TICK

  val TICKS_FOR_AUTOIO = 20
  val AMT_PER_AUTOIO   = 1
  val VOL_PER_AUTOIO   = 1

  val INPUT_INV_KEY = "Input"
  val NONE_KEY      = "None"

  val NANITE_TANK_KEY = "Tank"
  val NONE_TANK_KEY   = "None"

  val TASK_NBT                = "Task"
  val ITEM_SIDED_CONFIG_NBT   = "ItemConfig"
  val NANITE_SIDED_CONFIG_NBT = "NaniteConfig"
  val TICKS_NBT               = "Ticks"

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

class TileNaniteExtractor extends TileEntityBase with TileInventory with PowerLeafNode with TileNaniteStorage {
  private val task: ExtractTask  = new ExtractTask(null)
  private val sidedStorageConfig = new SidedItemStorageConfiguration({
    case EnumFacing.UP | EnumFacing.SOUTH => INPUT_INV_KEY
    case _ => NONE_KEY
  },
  Map(NONE_KEY -> IItemStorage.Empty,
    INPUT_INV_KEY -> storage),
  () => world.getBlockState(pos).getValue(BlockFacing.FACING))
  private val sidedNaniteConfig  = new SidedNaniteStorageConfiguration(_ => NANITE_TANK_KEY,
    Map(NONE_TANK_KEY -> null,
      NANITE_TANK_KEY -> naniteStorageTank),
    () => world.getBlockState(pos).getValue(BlockFacing.FACING))
  var ticks = 0

  override def defaultBattery = new PowerBattery(5000)

  override def storageType: PowerStorageNodeType = PowerStorageNodeType.CONSUMER

  override def leafTransferRate: Double = 50d

  override def connectionRadius: Float = 8f

  override def defaultStorageTank: NaniteTank = new NaniteTank(50)

  override def defaultStorage: IItemStorage = new ItemStorageArray(1) {
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
    CybermaterialRegistry.getNaniteFromItem(item.getItem, item.getItemDamage).isDefined
  }

  override def hasGUI = true

  override def getGuiID = GuiIDs.TileNaniteExtractorID

  override def serverUpdate(): Unit = {
    super.serverUpdate()

    ticks = TileEntityUtils.incrementTicks(ticks, TICKS_FOR_AUTOIO)
    TileEntityUtils.checkDoItemInputIO(this, sidedStorageConfig, ticks, AMT_PER_AUTOIO)
    TileEntityUtils.checkDoNaniteInputIO(this, sidedNaniteConfig, ticks, VOL_PER_AUTOIO)

    if (task.stack == null) {
      val item = storage(0)
      if (!item.isEmpty) {
        val ins = storage.split(0, 1)
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

        task.stack = storageTank.fill(stack, true)
        if (task.stack == null) {
          task.reset()
        }
      }
    }

    TileEntityUtils.checkDoNaniteOutputIO(this, sidedNaniteConfig, ticks, VOL_PER_AUTOIO)
    TileEntityUtils.checkDoItemOutputIO(this, sidedStorageConfig, ticks, AMT_PER_AUTOIO)
  }

  def setProgress(progress: Double): Unit = {
    task.progress = progress
  }

  def getProgress = task.progress

  def getProgressMax = task.adjustedMax(0)

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = (capability, facing) match {
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.ITEM_STORAGE_CONFIGURABLE => true
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.NANITE_STORAGE_CONFIGURABLE => true
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.NANITE_STORAGE_TANK => true
    case _ => super.hasCapability(capability, facing)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = (capability, facing) match {
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.ITEM_STORAGE_CONFIGURABLE => sidedStorageConfig.asInstanceOf[T]
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.NANITE_STORAGE_CONFIGURABLE => sidedNaniteConfig.asInstanceOf[T]
    case (cap, null) if cap == com.itszuvalex.femtocraft.api.Capabilities.NANITE_STORAGE_TANK => naniteStorageTank.asInstanceOf[T]
    case (_, null) => super.getCapability(capability, facing)
    case (cap, _) if cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => Converter.IItemHandlerModifiableFromIItemStorage(sidedStorageConfig.getStorageForGlobalFacing(facing)).asInstanceOf[T]
    case (cap, _) if cap == Capabilities.ITEM_STORAGE => sidedStorageConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.NANITE_STORAGE_TANK => sidedNaniteConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case _ => super.getCapability(capability, facing)
  }

  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)
    task.deserializeNBT(nbt.getCompoundTag(TASK_NBT))
    if (nbt.hasKey(ITEM_SIDED_CONFIG_NBT))
      sidedStorageConfig.deserializeNBT(nbt.getCompoundTag(ITEM_SIDED_CONFIG_NBT))
    if (nbt.hasKey(NANITE_SIDED_CONFIG_NBT))
      sidedNaniteConfig.deserializeNBT(nbt.getCompoundTag(NANITE_SIDED_CONFIG_NBT))
    ticks = nbt.getInteger(TICKS_NBT)
  }

  override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(nbt)
    nbt.setTag(TASK_NBT, task.serializeNBT())
    nbt.setTag(ITEM_SIDED_CONFIG_NBT, sidedStorageConfig.serializeNBT())
    nbt.setTag(NANITE_SIDED_CONFIG_NBT, sidedNaniteConfig.serializeNBT())
    nbt.setInteger(TICKS_NBT, ticks)
    nbt
  }
}

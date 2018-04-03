package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.nanite.NaniteTank
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.industry.NaniteInfusionRecipeRegistry
import com.itszuvalex.femtocraft.industry.tile.TileNaniteInfuser.InfuseTask._
import com.itszuvalex.femtocraft.industry.tile.TileNaniteInfuser._
import com.itszuvalex.femtocraft.nanite.{SidedNaniteStorageConfiguration, TileNaniteStorage}
import com.itszuvalex.femtocraft.power.node.PowerLeafNode
import com.itszuvalex.femtocraft.util.data.{DataInt, DataSerializable, TileDataSpec}
import com.itszuvalex.femtocraft.util.{TileEntityUtils, Wrapper}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray, ItemStorageSlice}
import com.itszuvalex.itszulib.api.wrappers.{Converter, IItemStack, PowerBattery}
import com.itszuvalex.itszulib.core.traits.tile.{BlockFacing, TileInventory}
import com.itszuvalex.itszulib.core.{SidedItemStorageConfiguration, TileEntityBase}
import com.itszuvalex.itszulib.util.Task
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler

object TileNaniteInfuser {
  val TICKS_REQ      = 20 * 20
  val POWER_PER_TICK = 10
  val POWER_REQ      = TICKS_REQ * POWER_PER_TICK

  val INPUT_INV_KEY  = "Input"
  val OUTPUT_INV_KEY = "Output"
  val NONE_INV_KEY   = "None"

  val NANITE_TANK_KEY = "Tank"
  val NONE_TANK_KEY   = "None"

  val TASK_NBT                = "Task"
  val ITEM_SIDED_CONFIG_NBT   = "ItemConfig"
  val NANITE_SIDED_CONFIG_NBT = "NaniteConfig"
  val TICKS_NBT               = "Ticks"

  val TICKS_FOR_AUTOIO = 20
  val AMT_PER_AUTOIO   = 1
  val VOL_PER_AUTOIO   = 1

  object InfuseTask {
    val INFUSING_STACK_NBT = "Infuse"
    val INFUSED_NBT        = "Infused"
  }

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

}

class TileNaniteInfuser extends TileEntityBase with TileInventory with TileDataSpec with PowerLeafNode with TileNaniteStorage {
  private                   val task         : InfuseTask   = new InfuseTask(IItemStack.Empty)
  @Wrapper(storage) private val inputStorage : IItemStorage = new ItemStorageSlice(storage, Array(0))
  @Wrapper(storage) private val outputStorage: IItemStorage = new ItemStorageSlice(storage, Array(1))
  private                   val sidedStorageConfig          = new SidedItemStorageConfiguration({
    case EnumFacing.UP | EnumFacing.SOUTH => INPUT_INV_KEY
    case EnumFacing.DOWN | EnumFacing.EAST | EnumFacing.WEST | EnumFacing.NORTH => OUTPUT_INV_KEY
    case _ => NONE_INV_KEY
  },
  Map(NONE_INV_KEY -> IItemStorage.Empty,
    INPUT_INV_KEY -> inputStorage,
    OUTPUT_INV_KEY -> outputStorage),
  () => world.getBlockState(pos).getValue(BlockFacing.FACING))
  private                   val sidedNaniteConfig           = new SidedNaniteStorageConfiguration(_ => NANITE_TANK_KEY,
    Map(NONE_TANK_KEY -> null,
      NANITE_TANK_KEY -> naniteStorageTank),
    () => world.getBlockState(pos).getValue(BlockFacing.FACING))
  var ticks = 0

  descriptionDataSpec ++= Array(
    new DataSerializable[NBTTagCompound](ITEM_SIDED_CONFIG_NBT, sidedStorageConfig),
    new DataSerializable[NBTTagCompound](NANITE_SIDED_CONFIG_NBT, sidedNaniteConfig)
  )
  saveDataSpec ++= Array(
    new DataSerializable[NBTTagCompound](ITEM_SIDED_CONFIG_NBT, sidedStorageConfig),
    new DataSerializable[NBTTagCompound](NANITE_SIDED_CONFIG_NBT, sidedNaniteConfig),
    new DataSerializable[NBTTagCompound](TASK_NBT, task),
    new DataInt(TICKS_NBT, ticks _, ticks_=)
  )

  override def defaultBattery = new PowerBattery(4000)

  override def powerStorageNodeType: PowerStorageNodeType = PowerStorageNodeType.CONSUMER

  override def powerStorageTransferRate: Double = 50d

  override def hasDescription: Boolean = true

  override def getMod: AnyRef = Femtocraft

  override def defaultStorage: IItemStorage = new ItemStorageArray(2) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = {
      isItemValidForSlot(i, stack.toMinecraft)
    }
  }

  override def getFieldCount: Int = 0

  override def setField(id: Int, value: Int): Unit = {}

  override def getField(id: Int): Int = 0

  override def defaultStorageTank: NaniteTank = new NaniteTank(50)

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean = {
    (slot, item) match {
      case (_, null) => true
      case (_, b) if b.isEmpty => true
      case (0, _) => NaniteInfusionRecipeRegistry.getMatchingRecipe(Converter.IItemStackFromItemStack(item)).isDefined
      case _ => false
    }
  }

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileNaniteInfuserID

  override def serverUpdate(): Unit = {
    super.serverUpdate()

    ticks = TileEntityUtils.incrementTicks(ticks, TICKS_FOR_AUTOIO)
    TileEntityUtils.checkDoItemInputIO(this, sidedStorageConfig, ticks, AMT_PER_AUTOIO)
    TileEntityUtils.checkDoNaniteInputIO(this, sidedNaniteConfig, ticks, VOL_PER_AUTOIO)

    if (task.stack == null || task.stack.isEmpty) {
      val item = storage(0)
      if (!item.isEmpty) {
        val recipe = NaniteInfusionRecipeRegistry.getMatchingRecipe(item)
        if (recipe.isDefined) {
          val r = recipe.get
          if (naniteStorageTank.containsNanite(r.nanitesRequired.nanite)) {
            val fakeDrain = naniteStorageTank.drain(r.nanitesRequired.nanite, r.nanitesRequired.volume, false)
            if (fakeDrain != null && fakeDrain.volume == r.nanitesRequired.volume) {
              naniteStorageTank.drain(r.nanitesRequired.nanite, r.nanitesRequired.volume, true)
              val ins = storage.split(0, 1)
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
        task.stack = storage.insert(1, insertItem)
        if (task.stack == null || task.stack.isEmpty)
          task.reset()
      }
    }

    TileEntityUtils.checkDoNaniteOutputIO(this, sidedNaniteConfig, ticks, VOL_PER_AUTOIO)
    TileEntityUtils.checkDoItemOutputIO(this, sidedStorageConfig, ticks, AMT_PER_AUTOIO)
  }

  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: EnumFacing): Boolean = {
    if (hasGUI) par5EntityPlayer.openGui(getMod, getGuiID, world, pos.getX, pos.getY, pos.getZ)
    hasGUI
  }

  def setProgress(progress: Double): Unit = {
    task.progress = progress
  }

  def getProgress = task.progress

  def getProgressMax = task.adjustedMax(0)

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = (capability, facing) match {
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.ITEM_STORAGE_CONFIGURABLE => true
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.NANITE_STORAGE_CONFIGURABLE => true
    case (cap, _) if cap == com.itszuvalex.femtocraft.api.Capabilities.TILE_NANITE_STORAGE_TANK => true
    case _ => super.hasCapability(capability, facing)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = (capability, facing) match {
    case (cap, _) if cap == Capabilities.ITEM_STORAGE_CONFIGURABLE => sidedStorageConfig.asInstanceOf[T]
    case (cap, _) if cap == Capabilities.NANITE_STORAGE_CONFIGURABLE => sidedNaniteConfig.asInstanceOf[T]
    case (cap, null) if cap == Capabilities.TILE_NANITE_STORAGE_TANK => naniteStorageTank.asInstanceOf[T]
    case (_, null) => super.getCapability(capability, facing)
    case (cap, _) if cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => Converter.IItemHandlerModifiableFromIItemStorage(sidedStorageConfig.getStorageForGlobalFacing(facing)).asInstanceOf[T]
    case (cap, _) if cap == ItszuLibCapabilities.ITEM_STORAGE => sidedStorageConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case (cap, _) if cap == Capabilities.TILE_NANITE_STORAGE_TANK => sidedNaniteConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case _ => super.getCapability(capability, facing)
  }
}

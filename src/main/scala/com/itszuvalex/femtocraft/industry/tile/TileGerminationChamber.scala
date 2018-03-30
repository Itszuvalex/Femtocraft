package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power._
import com.itszuvalex.femtocraft.industry._
import com.itszuvalex.femtocraft.industry.multiblocks.MultiblockGerminationChamber
import com.itszuvalex.femtocraft.industry.tile.TileGerminationChamber._
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.util.data._
import com.itszuvalex.femtocraft.util.{TileEntityUtils, Wrapper}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.multiblock.MultiBlockInfo
import com.itszuvalex.itszulib.api.storage._
import com.itszuvalex.itszulib.api.wrappers.{Converter, IBattery, IItemStack, PowerBattery}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import com.itszuvalex.itszulib.util.Task
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.items.CapabilityItemHandler

object TileGerminationChamber {
  val TICKS_FOR_AUTOIO = 20
  val POWER_PER_TICK   = 40

  val TANK_SIZE    = 10000
  val BATTERY_SIZE = 30000

  val INPUT_INV_KEY  = "Input"
  val OUTPUT_INV_KEY = "Output"
  val NONE_INV_KEY   = "None"

  val ITEMS_PER_AUTOIO = 1
  val FLUID_PER_AUTOIO = 250

  val TANK_KEY      = "Tank"
  val NONE_TANK_KEY = "None"

  val TASK_NBT               = "Task"
  val TANK_NBT               = "Tank"
  val ITEMS_NBT              = "Items"
  val ITEM_SIDED_CONFIG_NBT  = "ItemConfig"
  val FLUID_SIDED_CONFIG_NBT = "FluidConfig"
  val TICKS_NBT              = "Ticks"
  val STATE_NBT              = "State"
  val BATTERY_NBT            = "Battery"
  val LEAF_NODE_NBT          = "LeafNode"
  val MULTIBLOCK_INFO_NBT    = "Multiblock"

  class GerminationTask extends Task() {
    var stack : IItemStack               = IItemStack.Empty
    var recipe: GerminationChamberRecipe = _

    override def reset(): Unit = {
      super.reset()
      stack = IItemStack.Empty
      recipe = null
    }
  }

  class GerminationChamberState(val tile: TileEntityBase) extends DataSpec {
                      val battery                 : IBattery                     = new PowerBattery(BATTERY_SIZE)
                      val tank                    : IFluidStorage                = new FluidStorage(TANK_SIZE)
                      val storage                 : IItemStorage                 = new ItemStorageArray(2)
    @Wrapper(storage) val inputStorage            : IItemStorage                 = new ItemStorageSlice(storage, Array(0))
    @Wrapper(storage) val outputStorage           : IItemStorage                 = new ItemStorageSlice(storage, Array(1))
                      val task                    : GerminationTask              = new GerminationTask
                      val powerStorageNodeDelegate: PowerStorageNodeDelegate     = new PowerStorageNodeDelegate(tile, battery _, PowerStorageNodeType.CONSUMER, () => 40d)
                      val powerLeafNodeDelegate   : PowerNetworkLeafNodeDelegate = new PowerNetworkLeafNodeDelegate(tile, () => 8f, battery _, PowerStorageNodeType.CONSUMER,
                        PowerNetworkLeafNodeDelegate.INHERIT_TRANSFER_FROM_PARENT(powerLeafNodeDelegate, 40d), () => powerStorageNodeDelegate.changeForLastTick
                      )

    dataSpec ++= Array(
      new DataSerializable[NBTTagCompound](BATTERY_NBT, battery),
      new DataSerializable[NBTTagCompound](TANK_NBT, tank),
      new DataSerializable[NBTTagCompound](ITEMS_NBT, storage),
      new DataSerializable[NBTTagCompound](TASK_NBT, task),
      new DataSerializable[NBTTagCompound](LEAF_NODE_NBT, powerLeafNodeDelegate)
    )
  }

}

class TileGerminationChamber extends TileEntityBase with TileDataSpec with MultiBlockComponent with IPowerLeafNode with IPowerStorageNode {
  override val info: MultiBlockInfo = new MultiBlockInfo {
    override def formMultiBlock(loc: Loc4): Boolean = {
      val ret = super.formMultiBlock(loc)
      if (ret && isController(getLoc))
        PowerManager.addLeaf(getCapability(Capabilities.TILE_POWER_LEAF_NODE, null))
      setUpdate()
      ret
    }
  }

  private val state:
    MultiblockStateHolder[GerminationChamberState, TileGerminationChamber] =
    new MultiblockStateHolder[GerminationChamberState, TileGerminationChamber](this, () => new GerminationChamberState(this), info _, (a) => a.state)
  val tank   : IFluidStorage = new DynamicIFluidStorage(() => state.get.map(x => x.tank).getOrElse(IFluidStorage.Empty))
  val storage: IItemStorage  = new DynamicIItemStorage(() => state.get.map(x => x.storage).getOrElse(IItemStorage.Empty))
  @Wrapper(storage) private val inputStorage : IItemStorage = new DynamicIItemStorage(() => state.get.map(x => x.inputStorage).getOrElse(IItemStorage.Empty))
  @Wrapper(storage) private val outputStorage: IItemStorage = new DynamicIItemStorage(() => state.get.map(x => x.outputStorage).getOrElse(IItemStorage.Empty))
  private                   var ticks                       = 0

  private val sidedStorageConfig = new MultiblockSidedItemStorageConfiguration(
    getLoc _, info _, NONE_INV_KEY, _ => INPUT_INV_KEY,
    Map(NONE_INV_KEY -> IItemStorage.Empty,
      INPUT_INV_KEY -> inputStorage,
      OUTPUT_INV_KEY -> outputStorage),
    () => EnumFacing.NORTH)

  private val sidedFluidConfig = new MultiblockSidedFluidStorageConfiguration(
    getLoc _, info _, NONE_TANK_KEY, _ => TANK_KEY,
    Map(NONE_TANK_KEY -> IFluidStorage.Empty,
      TANK_KEY -> tank),
    () => EnumFacing.NORTH
  )

  descriptionDataSpec ++= Array(
    new DataSerializable[NBTTagCompound](ITEM_SIDED_CONFIG_NBT, sidedStorageConfig),
    new DataSerializable[NBTTagCompound](FLUID_SIDED_CONFIG_NBT, sidedFluidConfig),
    new DataSerializable[NBTTagCompound](MULTIBLOCK_INFO_NBT, info)
  )
  descriptionDataSpec.onLoad = () => setRenderUpdate()
  saveDataSpec ++= Array(
    new DataSerializable[NBTTagCompound](ITEM_SIDED_CONFIG_NBT, sidedStorageConfig),
    new DataSerializable[NBTTagCompound](FLUID_SIDED_CONFIG_NBT, sidedFluidConfig),
    new MultiblockStateHolder.DataMultiblockState[GerminationChamberState](STATE_NBT, state),
    new DataSerializable[NBTTagCompound](MULTIBLOCK_INFO_NBT, info),
    new DataInt(TICKS_NBT, ticks _, ticks_=)
  )

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    ticks = TileEntityUtils.incrementTicks(ticks, TICKS_FOR_AUTOIO)
    TileEntityUtils.checkDoItemInputIO(this, sidedStorageConfig, ticks, ITEMS_PER_AUTOIO)
    TileEntityUtils.checkDoFluidInputIO(this, sidedFluidConfig, ticks, FLUID_PER_AUTOIO)
    TileEntityUtils.checkDoItemOutputIO(this, sidedStorageConfig, ticks, ITEMS_PER_AUTOIO)
    TileEntityUtils.checkDoFluidOutputIO(this, sidedFluidConfig, ticks, FLUID_PER_AUTOIO)
  }

  override def hasGUI: Boolean = true

  override def hasDescription: Boolean = true

  override def getMod: AnyRef = Femtocraft

  override def getGuiID: Int = GuiIDs.TileGerminationChamberID

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = (capability, facing) match {
    case _ if capability == com.itszuvalex.itszulib.api.Capabilities.TILE_MULTIBLOCK => true
    case _ if capability == Capabilities.ITEM_STORAGE_CONFIGURABLE => true
    case _ if capability == Capabilities.FLUID_STORAGE_CONFIGURABLE => true
    case _ if capability == com.itszuvalex.itszulib.api.Capabilities.ITEM_STORAGE => true
    case _ if capability == com.itszuvalex.itszulib.api.Capabilities.FLUID_STORAGE => true
    case _ if capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => true
    case _ if capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => true
    case _ if capability == Capabilities.POWER_STORAGE => true
    case _ if capability == Capabilities.TILE_POWER_LEAF_NODE => isController
    case _ if capability == Capabilities.TILE_POWER_STORAGE_NODE => isController
    case _ => super.hasCapability(capability, facing)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = (capability, facing) match {
    case _ if capability == com.itszuvalex.itszulib.api.Capabilities.TILE_MULTIBLOCK => info.asInstanceOf[T]
    case _ if capability == Capabilities.ITEM_STORAGE_CONFIGURABLE => sidedStorageConfig.asInstanceOf[T]
    case _ if capability == Capabilities.FLUID_STORAGE_CONFIGURABLE => sidedFluidConfig.asInstanceOf[T]
    case _ if capability == Capabilities.POWER_STORAGE => state.get.map(x => x.battery).getOrElse(BatteryEmpty.Empty).asInstanceOf[T]
    case _ if capability == Capabilities.TILE_POWER_LEAF_NODE => state.get.map(x => x.powerLeafNodeDelegate).get.asInstanceOf[T]
    case _ if capability == Capabilities.TILE_POWER_STORAGE_NODE => state.get.map(x => x.powerStorageNodeDelegate).get.asInstanceOf[T]
    case (cap, null) if cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => Converter.IItemHandlerModifiableFromIItemStorage(storage).asInstanceOf[T]
    case (cap, null) if cap == com.itszuvalex.itszulib.api.Capabilities.ITEM_STORAGE => storage.asInstanceOf[T]
    case (cap, null) if cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => tank.asInstanceOf[T]
    case (cap, null) if cap == com.itszuvalex.itszulib.api.Capabilities.FLUID_STORAGE => tank.asInstanceOf[T]
    case (_, null) => super.getCapability(capability, facing)
    case (cap, _) if cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => Converter.IItemHandlerModifiableFromIItemStorage(sidedStorageConfig.getStorageForGlobalFacing(facing)).asInstanceOf[T]
    case (cap, _) if cap == com.itszuvalex.itszulib.api.Capabilities.ITEM_STORAGE => sidedStorageConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case (cap, _) if cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => sidedFluidConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case (cap, _) if cap == com.itszuvalex.itszulib.api.Capabilities.FLUID_STORAGE => sidedFluidConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case _ => super.getCapability(capability, facing)
  }

  override def battery: IBattery = getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery

  override def storageType: PowerStorageNodeType = getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).storageType

  override def transferRate: Double = getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).transferRate

  override def getStorageLoc: Loc4 = getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).getStorageLoc

  override def changeForLastTick: Double = getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).changeForLastTick

  override def connectionRadius: Float = getCapability(Capabilities.TILE_POWER_LEAF_NODE, null).connectionRadius

  override def getParent: Loc4 = getCapability(Capabilities.TILE_POWER_LEAF_NODE, null).getParent

  override def setParent(node: IPowerNetworkNode): Unit = getCapability(Capabilities.TILE_POWER_LEAF_NODE, null).setParent(node)

  override def onParentBroken(node: IPowerNetworkNode): Unit = getCapability(Capabilities.TILE_POWER_LEAF_NODE, null).onParentBroken(node)

  override def getRenderBoundingBox: AxisAlignedBB = {
    if (isController) {
      new AxisAlignedBB(getPos, getPos.add(MultiblockGerminationChamber.xSize, MultiblockGerminationChamber.ySize, MultiblockGerminationChamber.zSize))
    }
    else super.getRenderBoundingBox
  }

  override def onBlockBreak(): Unit = {
    if (getWorld.isRemote) return

    if (isController) {
      if (!world.isRemote) {
        state.get.foreach { a =>
          PowerManager.onLeafBroken(a.powerLeafNodeDelegate)
          PowerManager.removeLeaf(a.powerLeafNodeDelegate)
        }
      }
      FrameMultiblockRegistry.getMultiblock(MultiblockGerminationChamber.name)
        .foreach {
          _.onMultiblockBroken(getLoc)
        }
    }
    else {
      getInfo.cLoc.getWorld.foreach(_.setBlockToAir(getInfo.cLoc.getPos))
    }
  }

  override def onLoad(): Unit = {
    super.onLoad()
    if (getWorld.isRemote) return
    if (isController) state.get.foreach(a => PowerManager.addLeaf(a.powerLeafNodeDelegate))
  }

  override def validate(): Unit = {
    super.validate()
    if (getWorld.isRemote) return
    if (isController) state.get.foreach(a => PowerManager.addLeaf(a.powerLeafNodeDelegate))
  }

  override def invalidate(): Unit = {
    super.invalidate()
    if (getWorld.isRemote) return
    if (isController) state.get.foreach(a => PowerManager.removeLeaf(a.powerLeafNodeDelegate))
  }

  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: EnumFacing): Boolean = {
    if (hasGUI && info.isValidMultiBlock) {
      super.onSideActivate(par5EntityPlayer, side)
    }
    else false
  }

}

package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power._
import com.itszuvalex.femtocraft.industry._
import com.itszuvalex.femtocraft.industry.multiblocks.MultiblockGerminationChamber
import com.itszuvalex.femtocraft.industry.tile.TileGerminationChamber._
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.power.render.TileBeamRenderOffset
import com.itszuvalex.femtocraft.util.Wrapper
import com.itszuvalex.femtocraft.util.data._
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.multiblock.MultiBlockInfo
import com.itszuvalex.itszulib.api.storage._
import com.itszuvalex.itszulib.api.wrappers.{Converter, IItemStack}
import com.itszuvalex.itszulib.api.{ItszuLibCapabilities, ItszuLibModules}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import com.itszuvalex.itszulib.render.Vector3
import com.itszuvalex.itszulib.util.{Color, Task, TileEntityUtils}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.items.CapabilityItemHandler

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

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
    var stack    : IItemStack   = IItemStack.Empty
    var results  : IItemStorage = IItemStorage.Empty
    var completed: Boolean      = false

    override def reset(): Unit = {
      super.reset()
      stack = IItemStack.Empty
      completed = false
      results = IItemStorage.Empty
      baseGoal = 1d
      minTicks = 1
    }

    def generateResults(germinationChamberRecipe: GerminationChamberRecipe): Unit = {
      val size = germinationChamberRecipe.results.size
      results = new ItemStorageArray(size)
      germinationChamberRecipe.results.zipWithIndex.foreach { result =>
        val resultStack = result._1._1.copy()
        val range       = result._1._2
        resultStack.stackSize = Random.nextInt(range._2 - range._1) + range._1
        results(result._2) = resultStack
      }
    }

    override def deserializeNBT(t: NBTTagCompound): Unit = {
      super.deserializeNBT(t)
      if (t.hasKey(GerminationTask.ITEM_NBT))
        stack = IItemStack.createFromNBT(t.getCompoundTag(GerminationTask.ITEM_NBT))
      completed = t.getBoolean(GerminationTask.COMPLETED_NBT)
      if (t.hasKey(GerminationTask.RESULTS_NBT)) {
        val size = t.getInteger(GerminationTask.RESULTS_SIZE_NBT)
        results = new ItemStorageArray(size)
        results.deserializeNBT(t.getCompoundTag(GerminationTask.RESULTS_NBT))
      }
    }

    override def serializeNBT(): NBTTagCompound = {
      val nbt = super.serializeNBT()
      if (!stack.isEmpty)
        nbt.setTag(GerminationTask.ITEM_NBT, stack.serializeNBT())
      nbt.setBoolean(GerminationTask.COMPLETED_NBT, completed)
      if (results.exists(!_.isEmpty)) {
        nbt.setInteger(GerminationTask.RESULTS_SIZE_NBT, results.length)
        nbt.setTag(GerminationTask.RESULTS_NBT, results.serializeNBT())
      }
      nbt
    }
  }

  class GerminationChamberState(val tile: TileEntityBase) extends DataSpec {
                      val battery                 : IBattery                     = new PowerBattery(BATTERY_SIZE)
                      val tank                    : IFluidStorage                = new FluidStorage(TANK_SIZE)
                      val storage                 : IItemStorage                 = new ItemStorageArray(4) {
                        override def canInsert(i: Int, stack: IItemStack): Boolean = insertingOutput || (i == 0 && GerminationChamberRecipeRegistry.findMatchingRecipe(stack).isDefined)
                      }
    @Wrapper(storage) val inputStorage            : IItemStorage                 = new ItemStorageSlice(storage, Array(0))
    @Wrapper(storage) val outputStorage           : IItemStorage                 = new ItemStorageSlice(storage, Array(1, 2, 3))
                      val task                    : GerminationTask              = new GerminationTask
                      val powerStorageNodeDelegate: PowerStorageNodeDelegate     = new PowerStorageNodeDelegate(tile, battery _, PowerStorageNodeType.CONSUMER, () => 40d)
                      val powerLeafNodeDelegate   : PowerNetworkLeafNodeDelegate = new PowerNetworkLeafNodeDelegate(tile, () => 8f, battery _, PowerStorageNodeType.CONSUMER,
                                                                                                                    PowerNetworkLeafNodeDelegate.INHERIT_TRANSFER_FROM_PARENT(powerLeafNodeDelegate, 40d), () => powerStorageNodeDelegate.changeForLastTick
                                                                                                                    )
                      val descriptionSpec                                        = new DataSpecification(ArrayBuffer(
                        new DataSerializable[NBTTagCompound](LEAF_NODE_NBT, powerLeafNodeDelegate)
                        ))

    dataSpec ++= Array(
      new DataSerializable[NBTTagCompound](BATTERY_NBT, battery),
      new DataSerializable[NBTTagCompound](TANK_NBT, tank),
      new DataSerializable[NBTTagCompound](ITEMS_NBT, storage),
      new DataSerializable[NBTTagCompound](TASK_NBT, task),
      new DataSerializable[NBTTagCompound](LEAF_NODE_NBT, powerLeafNodeDelegate)
      )
    var insertingOutput: Boolean = false
  }

  object GerminationTask {
    val ITEM_NBT         = "Item"
    val COMPLETED_NBT    = "Completed"
    val RESULTS_SIZE_NBT = "ResultsSize"
    val RESULTS_NBT      = "Results"
  }

}

class TileGerminationChamber extends TileEntityBase with TileDataSpec with MultiBlockComponent with IPowerLeafNode with IPowerStorageNode with TileBeamRenderOffset {
  override val info: MultiBlockInfo = new MultiBlockInfo {

    override def formMultiBlock(loc: Loc4, cloc: Loc4): Boolean = {
      val ret = super.formMultiBlock(loc, cloc)
      if (isController)
        PowerManager.instance.addLeaf(getCapability(Capabilities.TILE_POWER_LEAF_NODE, null))
      setUpdate()
      ret
    }
  }
  val tank   : IFluidStorage = new DynamicIFluidStorage(() => state.get.map(x => x.tank).getOrElse(IFluidStorage.Empty))
  val storage: IItemStorage  = new DynamicIItemStorage(() => state.get.map(x => x.storage).getOrElse(IItemStorage.Empty))
  private                   val state        :
    MultiblockStateHolder[GerminationChamberState, TileGerminationChamber] =
    new MultiblockStateHolder[GerminationChamberState, TileGerminationChamber](this, () => new GerminationChamberState(this), info _, (a) => a.state)
  @Wrapper(storage) private val inputStorage : IItemStorage                = new DynamicIItemStorage(() => state.get.map(x => x.inputStorage).getOrElse(IItemStorage.Empty))
  @Wrapper(storage) private val outputStorage: IItemStorage                = new DynamicIItemStorage(() => state.get.map(x => x.outputStorage).getOrElse(IItemStorage.Empty))
  private                   val sidedStorageConfig                         = new MultiblockSidedItemStorageConfiguration(
    getLoc _, info _, NONE_INV_KEY, _ => INPUT_INV_KEY,
    Map(NONE_INV_KEY -> IItemStorage.Empty,
        INPUT_INV_KEY -> inputStorage,
        OUTPUT_INV_KEY -> outputStorage),
    () => EnumFacing.NORTH)
  private                   val sidedFluidConfig                           = new MultiblockSidedFluidStorageConfiguration(
    getLoc _, info _, NONE_TANK_KEY, _ => TANK_KEY,
    Map(NONE_TANK_KEY -> IFluidStorage.Empty,
        TANK_KEY -> tank),
    () => EnumFacing.NORTH
    )
  private var ticks                                                        = 0

  descriptionDataSpec ++= Array(
    new DataSerializable[NBTTagCompound](ITEM_SIDED_CONFIG_NBT, sidedStorageConfig),
    new DataSerializable[NBTTagCompound](FLUID_SIDED_CONFIG_NBT, sidedFluidConfig),
    new DataSerializable[NBTTagCompound](MULTIBLOCK_INFO_NBT, info),
    new ConditionalData(() => state.hasState, new DataSerializable[NBTTagCompound](STATE_NBT, () => state.get.get.descriptionSpec))
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

    if (isController) controllerUpdate()

    TileEntityUtils.checkDoItemOutputIO(this, sidedStorageConfig, ticks, ITEMS_PER_AUTOIO)
    TileEntityUtils.checkDoFluidOutputIO(this, sidedFluidConfig, ticks, FLUID_PER_AUTOIO)
    if (isController) state.get.foreach(_.powerStorageNodeDelegate.updateServerTick())
  }

  private def controllerUpdate(): Unit = {
    val actualState = state.get.get
    val task        = actualState.task

    if (task.stack == null || task.stack.isEmpty) {
      val item = storage(0)
      if (!item.isEmpty) {
        val recipe = GerminationChamberRecipeRegistry.findMatchingRecipe(item)
        if (recipe.isDefined) {
          val r   = recipe.get
          val ins = storage.split(0, 1)
          task.reset()
          task.stack = ins
          task.minTicks = recipe.get.ticks
          task.baseGoal = recipe.get.ticks * recipe.get.powerPerTick
        }
      }
    }
    else {
      GerminationChamberRecipeRegistry.findMatchingRecipe(task.stack) match {
        case None =>
          task.reset()
        case Some(recipe) =>
          val fakeRemoval = actualState.tank.drain(Converter.IFluidStackFromFluidStack(new FluidStack(recipe.fluid, recipe.fluidPerTick)), false)
          if (fakeRemoval != null && fakeRemoval.amount == recipe.fluidPerTick) {
            actualState.tank.drain(Converter.IFluidStackFromFluidStack(new FluidStack(recipe.fluid, recipe.fluidPerTick)), true)
            battery.storage -= task.contribute(Math.min(task.powerPerTick(0, 0), battery.storage), 0, 0)
            if (task.completed(0)) {
              val item = task.stack
              if (item == null || item.isEmpty) {
                task.reset()
                return
              }

              if (!task.completed) {
                task.completed = true
                task.stack = IItemStack.Empty
                task.generateResults(recipe)
              }
            }

            if (task.completed && task.results.exists(!_.isEmpty)) {
              actualState.insertingOutput = true
              task.results.transferIntoStorage(actualState.outputStorage, Int.MaxValue)
              actualState.insertingOutput = false

              // Will clear the stack once we successfully insert the result item or set stack to the finished result
              if (task.results.forall(_.isEmpty))
                task.reset()
            }
          }
      }
    }
  }

  override def battery: IBattery = getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery

  override def hasDescription: Boolean = true

  override def getMod: AnyRef = Femtocraft

  override def getGuiID: Int = GuiIDs.TileGerminationChamberID

  override def shouldRenderInPass(pass: Int): Boolean = pass == 0 || pass == 1

  def getProgress = state.get.map(_.task.progress).getOrElse(0d)

  def setProgress(progress: Double): Unit = {
    state.get.foreach(_.task.progress = progress)
  }

  def getProgressMax = state.get.map(_.task.adjustedMax(0)).getOrElse(0d)

  def getBaseGoal: Double = state.get.map(_.task.baseGoal).getOrElse(0d)

  def setBaseGoal(goal: Double): Unit = state.get.foreach(_.task.baseGoal = goal)

  def getTicksMax: Int = state.get.map(_.task.minTicks).getOrElse(0)

  def setTicksMax(ticks: Int) = state.get.foreach(_.task.minTicks = ticks)

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = (capability, facing) match {
    case _ if capability == ItszuLibCapabilities.TILE_MULTIBLOCK => true
    case _ if capability == Capabilities.ITEM_STORAGE_CONFIGURABLE => true
    case _ if capability == Capabilities.FLUID_STORAGE_CONFIGURABLE => true
    case _ if capability == ItszuLibCapabilities.ITEM_STORAGE => true
    case _ if capability == ItszuLibCapabilities.FLUID_STORAGE => true
    case _ if capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => true
    case _ if capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => true
    case _ if capability == Capabilities.POWER_STORAGE => true
    case _ if capability == Capabilities.TILE_POWER_LEAF_NODE => true
    case _ if capability == Capabilities.TILE_POWER_STORAGE_NODE => true
    case _ if capability == ItszuLibCapabilities.COLORABLE => true
    case _ => super.hasCapability(capability, facing)
  }

  override def storageType: PowerStorageNodeType = getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).storageType

  override def transferRate: Double = getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).transferRate

  override def getStorageLoc: Loc4 = getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).getStorageLoc

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = (capability, facing) match {
    case _ if capability == ItszuLibCapabilities.TILE_MULTIBLOCK => info.asInstanceOf[T]
    case _ if capability == Capabilities.ITEM_STORAGE_CONFIGURABLE => sidedStorageConfig.asInstanceOf[T]
    case _ if capability == Capabilities.FLUID_STORAGE_CONFIGURABLE => sidedFluidConfig.asInstanceOf[T]
    case _ if capability == Capabilities.POWER_STORAGE => state.get.map(x => x.battery).getOrElse(IBattery.Empty).asInstanceOf[T]
    case _ if capability == Capabilities.TILE_POWER_LEAF_NODE => state.get.map(x => x.powerLeafNodeDelegate).get.asInstanceOf[T]
    case _ if capability == Capabilities.TILE_POWER_STORAGE_NODE => state.get.map(x => x.powerStorageNodeDelegate).get.asInstanceOf[T]
    case _ if capability == ItszuLibCapabilities.COLORABLE => getColor.asInstanceOf[T]
    case (cap, null) if cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => Converter.IItemHandlerModifiableFromIItemStorage(storage).asInstanceOf[T]
    case (cap, null) if cap == ItszuLibCapabilities.ITEM_STORAGE => storage.asInstanceOf[T]
    case (cap, null) if cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => tank.asInstanceOf[T]
    case (cap, null) if cap == ItszuLibCapabilities.FLUID_STORAGE => tank.asInstanceOf[T]
    case (_, null) => super.getCapability(capability, facing)
    case (cap, _) if cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY => Converter.IItemHandlerModifiableFromIItemStorage(sidedStorageConfig.getStorageForGlobalFacing(facing)).asInstanceOf[T]
    case (cap, _) if cap == ItszuLibCapabilities.ITEM_STORAGE => sidedStorageConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case (cap, _) if cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY => sidedFluidConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case (cap, _) if cap == ItszuLibCapabilities.FLUID_STORAGE => sidedFluidConfig.getStorageForGlobalFacing(facing).asInstanceOf[T]
    case _ => super.getCapability(capability, facing)
  }

  def getColor: Color = state.get.map(_.powerLeafNodeDelegate).flatMap(_.parentLoc).flatMap(_.getITileEntity()).withFilter(_.hasModule(ItszuLibModules.COLORABLE, null)).map(_.getModule(ItszuLibModules.COLORABLE, null)).getOrElse(Color(0, 0, 0, 0))

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
        state.get.foreach {
          a =>
            PowerManager.instance.onLeafBroken(a.powerLeafNodeDelegate)
            PowerManager.instance.removeLeaf(a.powerLeafNodeDelegate)
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
    if (isController) state.get.foreach(a => PowerManager.instance.addLeaf(a.powerLeafNodeDelegate))
  }

  override def validate(): Unit = {
    super.validate()
    if (getWorld.isRemote) return
    if (isController) state.get.foreach(a => PowerManager.instance.addLeaf(a.powerLeafNodeDelegate))
  }

  override def invalidate(): Unit = {
    super.invalidate()
    if (getWorld.isRemote) return
    if (isController) state.get.foreach(a => PowerManager.instance.removeLeaf(a.powerLeafNodeDelegate))
  }

  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: EnumFacing): Boolean = {
    if (hasGUI && info.isValidMultiBlock) {
      super.onSideActivate(par5EntityPlayer, side)
    }
    else false
  }

  override def hasGUI: Boolean = true

  override def offset: Vector3 = Vector3(.5d, 0, .5d)

  override def isController(loc: Loc4): Boolean = info.isController(loc)
}

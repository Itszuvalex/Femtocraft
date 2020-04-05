package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power._
import com.itszuvalex.femtocraft.industry._
import com.itszuvalex.femtocraft.industry.multiblocks.MultiblockGerminationChamber
import com.itszuvalex.femtocraft.industry.tile.TileGerminationChamber._
import com.itszuvalex.femtocraft.power._
import com.itszuvalex.femtocraft.power.render.TileBeamRenderOffset
import com.itszuvalex.femtocraft.util.Wrapper
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.core.{IModule, Loc4, Module}
import com.itszuvalex.itszulib.api.multiblock.{MultiBlockInfo, MultiblockSidedFluidStorageConfiguration, MultiblockSidedItemStorageConfiguration, MultiblockStateHolder}
import com.itszuvalex.itszulib.api.storage._
import com.itszuvalex.itszulib.api.wrappers.{Converter, IItemStack, ITileEntity}
import com.itszuvalex.itszulib.core.TileEntityCoreTickable
import com.itszuvalex.itszulib.core.modules._
import com.itszuvalex.itszulib.render.Vector3
import com.itszuvalex.itszulib.util.Task
import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fluids.FluidStack

import scala.util.Random

object TileGerminationChamber {
  val MODULE: IModule[ModuleGerminationChamber] = Module.registerModule("ModuleGerminationChamber", null)
  val TICKS_FOR_AUTOIO                          = 20
  val POWER_PER_TICK                            = 40

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

  class GerminationChamberState(val tile: TileGerminationChamber) extends INBTSerializable[NBTTagCompound] {
                      val battery               : IBattery                = new PowerBattery(BATTERY_SIZE)
                      val tank                  : IFluidStorageModifiable = new FluidStorageArray(1, TANK_SIZE)
                      val storage               : IItemStorage            = new ItemStorageArray(4) {
                        override def canInsert(i: Int, stack: IItemStack): Boolean = insertingOutput || (i == 0 && GerminationChamberRecipeRegistry.findMatchingRecipe(stack).isDefined)
                      }
    @Wrapper(storage) val inputStorage          : IItemStorage            = new ItemStorageSlice(storage, Array(0))
    @Wrapper(storage) val outputStorage         : IItemStorage            = new ItemStorageSlice(storage, Array(1, 2, 3))
                      val task                  : GerminationTask         = new GerminationTask
                      val powerStorageNodeModule: ModulePowerStorageNode  = new ModulePowerStorageNode(tile, battery, PowerStorageNodeType.CONSUMER, () => 40d)
                      val powerLeafNodeModule   : ModulePowerLeafNode     = new ModulePowerLeafNode(tile, battery, PowerStorageNodeType.CONSUMER, transRate = () => 40d)

    def serializeDescriptionNBT(): NBTTagCompound = {
      val nbt         = new NBTTagCompound
      val leafNodeNBT = new NBTTagCompound
      powerLeafNodeModule.writeDescriptionNBT(leafNodeNBT)
      nbt.setTag(LEAF_NODE_NBT, leafNodeNBT)
      nbt
    }

    def deserializeDescriptionNBT(tag: NBTTagCompound): Unit = {
      powerLeafNodeModule.readDescriptionNBT(tag.getCompoundTag(LEAF_NODE_NBT))
    }

    override def serializeNBT(): NBTTagCompound = {
      val nbt = new NBTTagCompound
      nbt.setTag(BATTERY_NBT, battery.serializeNBT())
      nbt.setTag(TANK_NBT, tank.serializeNBT())
      nbt.setTag(ITEMS_NBT, storage.serializeNBT())
      nbt.setTag(TASK_NBT, task.serializeNBT())
      val leafNodeNBT = new NBTTagCompound
      powerLeafNodeModule.writeWorldNBT(leafNodeNBT)
      nbt.setTag(LEAF_NODE_NBT, leafNodeNBT)
      nbt
    }

    override def deserializeNBT(nbt: NBTTagCompound): Unit = {
      battery.deserializeNBT(nbt.getCompoundTag(BATTERY_NBT))
      tank.deserializeNBT(nbt.getCompoundTag(TANK_NBT))
      storage.deserializeNBT(nbt.getCompoundTag(ITEMS_NBT))
      task.deserializeNBT(nbt.getCompoundTag(TASK_NBT))
      powerLeafNodeModule.readWorldNBT(nbt.getCompoundTag(LEAF_NODE_NBT))
    }

    var insertingOutput: Boolean = false

    def getProgress: Double = task.progress

    def setProgress(progress: Double): Unit = {
      task.progress = progress
    }

    def getProgressMax: Double = task.adjustedMax(0)

    def getBaseGoal: Double = task.baseGoal

    def setBaseGoal(goal: Double): Unit = task.baseGoal = goal

    def getTicksMax: Int = task.minTicks

    def setTicksMax(ticks: Int): Unit = task.minTicks = ticks
  }

  object GerminationTask {
    val ITEM_NBT         = "Item"
    val COMPLETED_NBT    = "Completed"
    val RESULTS_SIZE_NBT = "ResultsSize"
    val RESULTS_NBT      = "Results"
  }

  class ModuleGerminationChamber(info: MultiBlockInfo, state: MultiblockStateHolder[GerminationChamberState, TileGerminationChamber]) extends TileEntityMultiblockTickableModule[ModuleGerminationChamber](info) {
    override def module: IModule[ModuleGerminationChamber] = MODULE

    override def hasDescriptionNBT: Boolean = true

    override def hasWorldNBT: Boolean = true


    override def writeDescriptionNBT(tag: NBTTagCompound): Unit = {
      if (info.isController)
        tag.setTag(STATE_NBT, state.get.get.serializeDescriptionNBT())
    }

    override def readDescriptionNBT(tag: NBTTagCompound): Unit = {
      if (info.isController)
        state.get.get.deserializeDescriptionNBT(tag.getCompoundTag(STATE_NBT))
    }

    override def writeWorldNBT(tag: NBTTagCompound): Unit = {
      if (info.isController)
        tag.setTag(STATE_NBT, state.get.get.serializeNBT())
    }

    override def readWorldNBT(tagCompound: NBTTagCompound): Unit = {
      if (info.isController)
        state.get.get.deserializeNBT(tagCompound.getCompoundTag(STATE_NBT))
    }

    override def onBlockBreak(core: ITileEntity, st: IBlockState): Unit = {
      if (info.isController) {
        if (!core.getIWorld.isRemote) {
          state.get.foreach {
            a => a.powerLeafNodeModule.onBlockBreak(core, st)
          }
        }
        FrameMultiblockRegistry.getMultiblock(MultiblockGerminationChamber.name)
                               .foreach {
                                 _.onMultiblockBroken(Loc4(core))
                               }
      }
      else {
        info.controller.flatMap(_.getWorld).foreach(_.setBlockToAir(info.controller.get.getPos))
      }
    }


    override def onLoad(tile: ITileEntity): Unit = {
      if (info.isController) state.get.foreach(_.powerLeafNodeModule.onLoad(tile))
    }

    override def invalidate(tile: ITileEntity): Unit = {
      if (info.isController) state.get.foreach(_.powerLeafNodeModule.invalidate(tile))
    }

    override def serverControllerUpdate(tile: ITileEntity): Unit = state.get match {
      case None =>
      case Some(s) =>
        val task = s.task

        if (task.stack == null || task.stack.isEmpty) {
          val item = s.storage.head
          if (!item.isEmpty) {
            val recipe = GerminationChamberRecipeRegistry.findMatchingRecipe(item)
            if (recipe.isDefined) {
              val r   = recipe.get
              val ins = s.storage.split(0, 1)
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
              val fakeRemoval = s.tank.drain(Converter.IFluidStackFromFluidStack(new FluidStack(recipe.fluid, recipe.fluidPerTick)), false)
              if (fakeRemoval != null && fakeRemoval.amount == recipe.fluidPerTick) {
                s.tank.drain(Converter.IFluidStackFromFluidStack(new FluidStack(recipe.fluid, recipe.fluidPerTick)), true)
                s.battery.storage -= task.contribute(Math.min(task.powerPerTick(0, 0), s.battery.storage), 0, 0)
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
                  s.insertingOutput = true
                  task.results.transferIntoStorage(s.outputStorage, Int.MaxValue)
                  s.insertingOutput = false

                  // Will clear the stack once we successfully insert the result item or set stack to the finished result
                  if (task.results.forall(_.isEmpty))
                    task.reset()
                }
              }
          }
        }
    }
  }

}

class TileGerminationChamber extends TileEntityCoreTickable with TileBeamRenderOffset {
  val info                   : MultiBlockInfo                                                         = new MultiBlockInfo {

    override def breakMultiBlock(loc: Loc4): Boolean = {
      val ret = super.breakMultiBlock(loc)
      if (isController)
        PowerManager.instance.removeLeaf(state.get.get.powerLeafNodeModule)
      setUpdate()
      ret
    }

    override def formMultiBlock(loc: Loc4, cloc: Loc4): Boolean = {
      val ret = super.formMultiBlock(loc, cloc)
      if (isController)
        PowerManager.instance.addLeaf(state.get.get.powerLeafNodeModule)
      setUpdate()
      ret
    }
  }
  val state                  : MultiblockStateHolder[GerminationChamberState, TileGerminationChamber] =
    new MultiblockStateHolder[GerminationChamberState, TileGerminationChamber](this, () => new GerminationChamberState(this), info _, _.state)
  val multiblockStorageModule: ModuleMultiblockIItemStorage                                           = new ModuleMultiblockIItemStorage(() => state.get.map(_.storage))
  private val inputStorage : IItemStorage = new DynamicIItemStorage(() => state.get.map(_.inputStorage).getOrElse(IItemStorage.Empty))
  private val outputStorage: IItemStorage = new DynamicIItemStorage(() => state.get.map(_.outputStorage).getOrElse(IItemStorage.Empty))
  private val sidedStorageConfig          = new MultiblockSidedItemStorageConfiguration(
    getLoc _, info _, NONE_INV_KEY, _ => INPUT_INV_KEY,
    Map(NONE_INV_KEY -> IItemStorage.Empty,
        INPUT_INV_KEY -> inputStorage,
        OUTPUT_INV_KEY -> outputStorage),
    () => EnumFacing.NORTH)
  val multiblockFluidModule: ModuleMultiblockIFluidStorage = new ModuleMultiblockIFluidStorage(() => state.get.map(_.tank))
  private val sidedFluidConfig = new MultiblockSidedFluidStorageConfiguration(
    getLoc _, info _, NONE_TANK_KEY, _ => TANK_KEY,
    Map(NONE_TANK_KEY -> IFluidStorage.Empty,
        TANK_KEY -> multiblockFluidModule.storage),
    () => EnumFacing.NORTH
    )
  val multiblockBatteryModule: ModuleMultiblockIBattery = new ModuleMultiblockIBattery(() => state.get.map(_.battery)) {
    override def module: IModule[IBattery] = ManagerModules.POWER_STORAGE
  }

  val internal = new ModuleGerminationChamber(info, state)

  addTileEntityModule(new ModuleMultiblockInfo(info))
  addTileEntityModule(multiblockStorageModule)
  addTileEntityModule(new ModuleIItemSidedConfiguration(sidedStorageConfig))
  addTileEntityModule(new ModuleIItemHandlerConverter)
  addTileEntityModule(multiblockFluidModule)
  addTileEntityModule(new ModuleIFluidSidedConfiguration(sidedFluidConfig))
  addTileEntityModule(new ModuleIFluidHandlerConverter)
  addTileEntityModule(multiblockBatteryModule)
  addTileEntityModule(new ModuleMultiblockGui(info, Femtocraft, () => GuiIDs.TileGerminationChamberID))
  addTileEntityModule(new ModuleMultiblockColor(() => state.get.map(_.powerLeafNodeModule).
                                                           flatMap(_.parentLoc).flatMap(_.getITileEntity()).flatMap(_.moduleOption(ItszuLibModules.COLORABLE, null))))
  addTileEntityModule(new ModuleMultiblockPowerLeafNode(info, () => state.get.get.powerLeafNodeModule))
  addTileEntityModule(new ModuleMultiblockPowerStorageNode(info, () => state.get.get.powerStorageNodeModule))
  addTileEntityModuleTickable(internal)
  addTileEntityModuleTickable(new ModuleIItemAutoIO(sidedStorageConfig))
  addTileEntityModuleTickable(new ModuleIFluidAutoIO(sidedFluidConfig))

  override def shouldRenderInPass(pass: Int): Boolean = pass == 0 || pass == 1

  override def getRenderBoundingBox: AxisAlignedBB = {
    if (info.isController) {
      new AxisAlignedBB(getPos, getPos.add(MultiblockGerminationChamber.xSize, MultiblockGerminationChamber.ySize, MultiblockGerminationChamber.zSize))
    }
    else super.getRenderBoundingBox
  }

  override def offset: Vector3 = Vector3(.5d, 0, .5d)
}


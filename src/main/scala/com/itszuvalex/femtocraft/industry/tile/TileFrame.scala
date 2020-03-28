package com.itszuvalex.femtocraft.industry.tile

import java.util.Random

import com.itszuvalex.femtocraft.industry.item.ItemFrame
import com.itszuvalex.femtocraft.industry.tile.TileFrame.{ModuleFrame, TileFrameState}
import com.itszuvalex.femtocraft.industry.{FrameMultiblockRegistry, FrameMultiblockRendererRegistry}
import com.itszuvalex.femtocraft.{FemtoItems, Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.{IModule, Loc4, Module}
import com.itszuvalex.itszulib.api.multiblock.{MultiBlockInfo, MultiblockStateHolder}
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.wrappers.{Converter, ITileEntity}
import com.itszuvalex.itszulib.core.TileEntityCoreTickable
import com.itszuvalex.itszulib.core.modules.{ModuleMultiblockGui, ModuleMultiblockIItemStorage, ModuleMultiblockInfo, TileEntityMultiblockTickableModule}
import com.itszuvalex.itszulib.util.{InventoryUtils, StorageUtils, TileEntityUtils}
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.common.util.INBTSerializable

import scala.collection.mutable

/**
  * Created by Christopher on 8/29/2015.
  */
object TileFrame {
  val MODULE: IModule[ModuleFrame] = Module.registerModule("ModuleFrame", null)
  val BUILDING_KEY                 = "Building"
  val RENDER_SETTINGS_KEY          = "RenderSettings"
  val MULTIBLOCK_KEY               = "Multiblock"
  val PROGRESS_KEY                 = "BuildProgress"
  val STATE_KEY                    = "State"
  val INFO_KEY                     = "Info"
  val TICKS_TO_CHECK               = 40
  var shouldDrop                   = true
  var shouldFullyRemove            = true

  def fullRender(bool: Boolean) = setRenderMarks(bool, 0, 0 until 20: _*)

  def fullRenderIndexes = (0 until 20).map(markIndexes)

  def markIndexes(mark: Int) = (mark / 12, (mark / 4) % 3, mark % 4)

  def getRenderMark(i: Int, j: Int, k: Int, renderInt: Int) = (renderInt & (1 << TileFrame.getSaveableIndentation(i, j, k))) > 0

  def getSaveableIndentation(i: Int, j: Int, k: Int) = {
    19 - (12 * i) - (4 * j) - k
  }

  def setRenderMark(bool: Boolean, i: Int, j: Int, k: Int, marker: Int): Int = {
    val num = marker
    val i1  = 1 << getSaveableIndentation(i, j, k)
    if (bool) num | i1
    else num & ~i1
  }

  def setRenderMarks(bool: Boolean, marker: Int, markers: Int*): Int = {
    var num = marker
    markers.foreach(mark => num = setRenderMark(bool, mark / 12, (mark / 4) % 3, mark % 4, num))
    num
  }

  def renderPieces(sizeX: Int, sizeY: Int, sizeZ: Int, locX: Int, locY: Int, locZ: Int) = {
    val MaxX = sizeX - 1
    val MaxY = sizeY - 1
    val MaxZ = sizeZ - 1
    (locX, locY, locZ) match {
      case (0, 0, 0) => setRenderMarks(true, 0, 4, 8, 11, 12, 16, 17, 19) // Corner
      case (`MaxX`, 0, 0) => setRenderMarks(true, 0, 5, 8, 9, 13, 16, 17, 18) // Corner
      case (0, `MaxY`, 0) => setRenderMarks(true, 0, 0, 3, 4, 12, 13, 15, 16) // Corner
      case (0, 0, `MaxZ`) => setRenderMarks(true, 0, 7, 10, 11, 15, 16, 18, 19) // Corner
      case (`MaxX`, `MaxY`, 0) => setRenderMarks(true, 0, 0, 1, 5, 12, 13, 14, 17) // Corner
      case (0, `MaxY`, `MaxZ`) => setRenderMarks(true, 0, 2, 3, 7, 12, 14, 15, 19) // Corner
      case (`MaxX`, 0, `MaxZ`) => setRenderMarks(true, 0, 6, 9, 10, 14, 17, 18, 19) // Corner
      case (`MaxX`, `MaxY`, `MaxZ`) => setRenderMarks(true, 0, 1, 2, 6, 13, 14, 15, 18) // Corner
      case (0, 0, _) => setRenderMarks(true, 0, 11, 16, 19) // East/West, N/S?, Down Edge
      case (0, `MaxY`, _) => setRenderMarks(true, 0, 3, 12, 15) // East/West, N/S?, Top Edge
      case (`MaxX`, 0, _) => setRenderMarks(true, 0, 9, 17, 18) // East/West, !N/S?, Down Edge
      case (`MaxX`, `MaxY`, _) => setRenderMarks(true, 0, 1, 13, 14) // East/West, !N/S, Top Edge
      case (0, _, 0) => setRenderMarks(true, 0, 4, 12, 16) // Vertical
      case (0, _, `MaxZ`) => setRenderMarks(true, 0, 7, 15, 19) // Vertical
      case (`MaxX`, _, 0) => setRenderMarks(true, 0, 5, 13, 17) // Vertical
      case (`MaxX`, _, `MaxZ`) => setRenderMarks(true, 0, 6, 14, 18) // Vertical
      case (_, 0, 0) => setRenderMarks(true, 0, 8, 16, 17) // Horiz
      case (_, 0, `MaxZ`) => setRenderMarks(true, 0, 10, 18, 19) // Horiz
      case (_, `MaxY`, 0) => setRenderMarks(true, 0, 0, 12, 13) // Horiz
      case (_, `MaxY`, `MaxZ`) => setRenderMarks(true, 0, 2, 14, 15) // Horiz
      case _ => fullRender(false)
    }
  }

  class TileFrameState(tile: ITileEntity) extends INBTSerializable[NBTTagCompound] {
    val storage                                         = new ItemStorageArray(9)
    var multiBlock           : String                   = null
    var renderProgress       : Int                      = 0
    var progress             : Int                      = 0
    // # of seconds * 20tps
    var totalMachineBuildTime: Int                      = 10 * 20
    var inProgressData       : mutable.Map[String, Any] = mutable.Map()
    var isBuilding           : Boolean                  = false
    var ticks                                           = 0

    def checkForRequiredItems(): Unit = {
      FrameMultiblockRegistry.getMultiblock(multiBlock) match {
        case Some(multi) =>
          val items  = multi.getRequiredResources
          val random = new Random
          if (StorageUtils.removeItemsFromStorage(storage, items, false)) {
            storage.foreach { item =>
              if (!tile.getIWorld.isRemote) InventoryUtils.dropItem(item, new Loc4(tile), random)
            }
            isBuilding = true
            tile.setUpdate()
          }
        case _ =>
      }
    }

    def isCurrentlyBuilding: Boolean = isBuilding

    def serializeDescriptionNBT(): NBTTagCompound = {
      val nbt = new NBTTagCompound
      nbt.setString(TileFrame.MULTIBLOCK_KEY, multiBlock)
      nbt.setInteger(TileFrame.PROGRESS_KEY, renderProgress)
      nbt.setBoolean(TileFrame.BUILDING_KEY, isBuilding)
      nbt
    }

    def deserializeDescriptionNBT(tag: NBTTagCompound): Unit = {
      multiBlock = tag.getString(TileFrame.MULTIBLOCK_KEY)
      renderProgress = tag.getInteger(TileFrame.PROGRESS_KEY)
      isBuilding = tag.getBoolean(TileFrame.BUILDING_KEY)
    }

    override def serializeNBT(): NBTTagCompound = {
      val nbt = new NBTTagCompound
      nbt.setTag(TileFrameState.STORAGE_NBT, storage.serializeNBT())
      nbt.setString(TileFrame.MULTIBLOCK_KEY, multiBlock)
      nbt.setInteger(TileFrame.PROGRESS_KEY, renderProgress)
      nbt.setBoolean(TileFrame.BUILDING_KEY, isBuilding)
      nbt
    }

    override def deserializeNBT(nbt: NBTTagCompound): Unit = {
      storage.deserializeNBT(nbt.getCompoundTag(TileFrameState.STORAGE_NBT))
      multiBlock = nbt.getString(TileFrame.MULTIBLOCK_KEY)
      renderProgress = nbt.getInteger(TileFrame.PROGRESS_KEY)
      isBuilding = nbt.getBoolean(TileFrame.BUILDING_KEY)
    }
  }

  object TileFrameState {
    val STORAGE_NBT = "Storage"
  }

  class ModuleFrame(tile: ITileEntity, info: MultiBlockInfo, state: MultiblockStateHolder[TileFrameState, TileFrame]) extends TileEntityMultiblockTickableModule[ModuleFrame](info) {
    var renderInt: Int = TileFrame.fullRender(true)

    def calculateRendering(sizeX: Int, sizeY: Int, sizeZ: Int, locX: Int, locY: Int, locZ: Int): Unit = {
      renderInt = TileFrame.renderPieces(sizeX, sizeY, sizeZ, locX, locY, locZ)
    }

    def calculateRendering(connectedDirs: Array[EnumFacing]): Unit = {
      renderInt = TileFrame.fullRender(true)
    }

    def getRenderMark(i: Int, j: Int, k: Int) = TileFrame.getRenderMark(i, j, k, renderInt)

    def setRenderMark(bool: Boolean, i: Int, j: Int, k: Int): Unit = {
      renderInt = TileFrame.setRenderMark(bool, i, j, k, renderInt)
    }

    override def serverControllerUpdate(tile: ITileEntity): Unit = {
      state.get match {
        case None =>
        case Some(s) =>
          if (!s.isBuilding) {
            s.ticks = TileEntityUtils.incrementTicks(s.ticks, TileFrame.TICKS_TO_CHECK)
            if (s.ticks == 0)
              s.checkForRequiredItems()
          }
          else {
            s.progress += 1
            if (s.progress >= s.totalMachineBuildTime) {
              FrameMultiblockRegistry.getMultiblock(s.multiBlock) match {
                case Some(multi) =>
                  TileFrame.shouldDrop = false
                  TileFrame.shouldFullyRemove = false
                  multi.formAtLocation(new Loc4(tile))
                  TileFrame.shouldFullyRemove = true
                  TileFrame.shouldDrop = true
                case _ =>
              }
            }
          }
      }
    }

    override def clientControllerUpdate(tile: ITileEntity): Unit = {
      state.get match {
        case None =>
        case Some(s) =>
          if (s.isBuilding) {
            s.renderProgress += 1
          }
      }
    }

    override def onBlockBreak(core: ITileEntity, sta: IBlockState): Unit = {
      if (core.getIWorld.isRemote) return

      if (TileFrame.shouldFullyRemove) {
        if (info.isController) {
          state.get match {
            case None =>
            case Some(s) =>
              val random = new Random
              FrameMultiblockRegistry.getMultiblock(s.multiBlock) match {
                case Some(multi) =>
                  multi.getTakenLocations(new Loc4(tile)).foreach { loc =>
                    tile.getIWorld.setBlockToAir(loc.getPos)
                    if (TileFrame.shouldDrop) {
                      val itemStack = Converter.IItemStackFromItemStack(new ItemStack(FemtoItems.itemFrame))
                      ItemFrame.setSelection(itemStack, s.multiBlock)
                      InventoryUtils.dropItem(itemStack, loc, random)
                    }
                  }
                  if (s.isBuilding && TileFrame.shouldDrop)
                    multi.getRequiredResources.foreach(i => InventoryUtils.dropItem(i, new Loc4(core), random))
                case _ =>
              }
              state.get.foreach(_.storage.foreach(i => InventoryUtils.dropItem(i, new Loc4(core), random)))
          }
        }
        else info.controller.flatMap(_.getITileEntity(true)) match {
          case Some(_: TileFrame) => tile.getIWorld.setBlockToAir(info.controller.get.getPos)
          case _ =>
        }
      }
    }

    override def module: IModule[ModuleFrame] = TileFrame.MODULE

    override def hasDescriptionNBT: Boolean = true

    override def hasWorldNBT: Boolean = true

    override def writeDescriptionNBT(tag: NBTTagCompound): Unit = {
      tag.setInteger(TileFrame.RENDER_SETTINGS_KEY, renderInt)
      if (info.isController) {
        tag.setTag(TileFrame.STATE_KEY, state.get.get.serializeDescriptionNBT())
      }
    }

    override def readDescriptionNBT(tag: NBTTagCompound): Unit = {
      renderInt = tag.getInteger(TileFrame.RENDER_SETTINGS_KEY)
      if (info.isController) {
        state.get.get.deserializeDescriptionNBT(tag.getCompoundTag(TileFrame.STATE_KEY))
      }
      tile.setRenderUpdate()
    }

    override def writeWorldNBT(tag: NBTTagCompound): Unit = {
      tag.setInteger(TileFrame.RENDER_SETTINGS_KEY, renderInt)
      if (info.isController) {
        tag.setTag(TileFrame.STATE_KEY, state.get.get.serializeNBT())
      }
    }

    override def readWorldNBT(tag: NBTTagCompound): Unit = {
      renderInt = tag.getInteger(TileFrame.RENDER_SETTINGS_KEY)
      if (info.isController) {
        state.get.get.deserializeNBT(tag.getCompoundTag(TileFrame.STATE_KEY))
      }
    }
  }

}

class TileFrame() extends TileEntityCoreTickable {
  var info : MultiBlockInfo                          = new MultiBlockInfo
  val state:
    MultiblockStateHolder[TileFrameState, TileFrame] =
    new MultiblockStateHolder[TileFrameState, TileFrame](this, () => new TileFrameState(this), info _, _.state)

  val multiblockStorageModule = new ModuleMultiblockIItemStorage(() => state.get.map(_.storage))
  val internal                = new ModuleFrame(this, info, state)

  // Don't think I want to expose this
  /*addTileEntityModule(new ModuleIItemStorage(storage) {
    override def hasWorldNBT: Boolean = false
  })*/
  addTileEntityModule(multiblockStorageModule)
  addTileEntityModule(new ModuleMultiblockInfo(info))
  addTileEntityModule(new ModuleMultiblockGui(info, Femtocraft, () => if (state.get.exists(_.isBuilding)) GuiIDs.TileFrameConstructingGuiID else GuiIDs.TileFrameMultiblockGuiID))
  addTileEntityModuleTickable(internal)

  override def getRenderBoundingBox: AxisAlignedBB = {
    if (info.isController) {
      state.get match {
        case None =>
        case Some(s) =>
          FrameMultiblockRegistry.getMultiblock(s.multiBlock) match {
            case Some(m) =>
              FrameMultiblockRendererRegistry.getRenderer(m.multiblockRenderID) match {
                case Some(r) =>
                  return new AxisAlignedBB(getPos, getPos.add(r.boundingBox._1, r.boundingBox._2, r.boundingBox._3))
                case _ =>
              }
            case _ =>
          }
      }
    }
    super.getRenderBoundingBox
  }
}

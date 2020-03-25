/*package com.itszuvalex.femtocraft.industry.tile

import java.util.Random

import com.itszuvalex.femtocraft.industry.item.ItemFrame
import com.itszuvalex.femtocraft.industry.tile.TileFrame.TileFrameState
import com.itszuvalex.femtocraft.industry.{FrameMultiblockRegistry, FrameMultiblockRendererRegistry, MultiblockStateHolder}
import com.itszuvalex.femtocraft.util.StorageUtils
import com.itszuvalex.femtocraft.util.data._
import com.itszuvalex.femtocraft.{FemtoItems, Femtocraft, GuiIDs, industry}
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.api.storage.{DynamicIItemStorage, IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import com.itszuvalex.itszulib.util.{InventoryUtils, TileEntityUtils}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable

import scala.collection.mutable

/**
  * Created by Christopher on 8/29/2015.
  */
object TileFrame {
  val BUILDING_KEY        = "Building"
  val RENDER_SETTINGS_KEY = "RenderSettings"
  val MULTIBLOCK_KEY      = "Multiblock"
  val PROGRESS_KEY        = "BuildProgress"
  val STATE_KEY           = "State"
  val INFO_KEY            = "Info"
  val TICKS_TO_CHECK      = 40
  var shouldDrop          = true
  var shouldFullyRemove   = true

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

  class TileFrameState() extends INBTSerializable[NBTTagCompound] {
    val storage = new ItemStorageArray(9)

    override def serializeNBT(): NBTTagCompound = {
      val nbt = new NBTTagCompound
      nbt.setTag(TileFrameState.STORAGE_NBT, storage.serializeNBT())
      nbt
    }

    override def deserializeNBT(nbt: NBTTagCompound): Unit = {
      storage.deserializeNBT(nbt.getCompoundTag(TileFrameState.STORAGE_NBT))
    }
  }

  object TileFrameState {
    val STORAGE_NBT = "Storage"
  }

}

class TileFrame() extends TileEntityBase with TileDataSpec with MultiBlockComponent {
  private val state:
    MultiblockStateHolder[TileFrameState, TileFrame] =
    new MultiblockStateHolder[TileFrameState, TileFrame](this, () => new TileFrameState(), info _, (a) => a.state)
  var renderInt                                       = TileFrame.fullRender(true)
  var multiBlock           : String                   = null
  var renderProgress       : Int                      = 0
  var progress             : Int                      = 0
  // # of seconds * 20tps
  var totalMachineBuildTime: Int                      = 10 * 20
  var inProgressData       : mutable.Map[String, Any] = mutable.Map()
  var isBuilding           : Boolean                  = false
  var ticks                                           = 0
  var storage              : IItemStorage             = new DynamicIItemStorage(() => state.get.map(_.storage).getOrElse(IItemStorage.Empty))

  descriptionDataSpec ++= Array(
    new DataInt(TileFrame.RENDER_SETTINGS_KEY, renderInt _, renderInt_=),
    new DataString(TileFrame.MULTIBLOCK_KEY, multiBlock _, multiBlock_=),
    new DataInt(TileFrame.PROGRESS_KEY, renderProgress _, renderProgress_=),
    new DataBool(TileFrame.BUILDING_KEY, isBuilding _, isBuilding_=),
    new DataSerializable[NBTTagCompound](TileFrame.INFO_KEY, info)
    )
  descriptionDataSpec.onLoad = () => setRenderUpdate()
  saveDataSpec ++= Array(
    new DataInt(TileFrame.RENDER_SETTINGS_KEY, renderInt _, renderInt_=),
    new DataString(TileFrame.MULTIBLOCK_KEY, multiBlock _, multiBlock_=),
    new DataInt(TileFrame.PROGRESS_KEY, renderProgress _, renderProgress_=),
    new DataBool(TileFrame.BUILDING_KEY, isBuilding _, isBuilding_=),
    new industry.MultiblockStateHolder.DataMultiblockState[TileFrameState](TileFrame.STATE_KEY, state),
    new DataSerializable[NBTTagCompound](TileFrame.INFO_KEY, info)
    )

  def calculateRendering(sizeX: Int, sizeY: Int, sizeZ: Int, locX: Int, locY: Int, locZ: Int): Unit = {
    renderInt = TileFrame.renderPieces(sizeX, sizeY, sizeZ, locX, locY, locZ)
  }

  def calculateRendering(connectedDirs: Array[EnumFacing]): Unit = {
    renderInt = TileFrame.fullRender(true)
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability match {
    case c if capability == ItszuLibCapabilities.TILE_MULTIBLOCK => true
    case _ => super.hasCapability(capability, facing)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = capability match {
    case c if capability == ItszuLibCapabilities.TILE_MULTIBLOCK => info.asInstanceOf[T]
    case _ => super.getCapability(capability, facing)
  }

  override def getRenderBoundingBox: AxisAlignedBB = {
    if (isController) {
      FrameMultiblockRegistry.getMultiblock(multiBlock) match {
        case Some(m) =>
          FrameMultiblockRendererRegistry.getRenderer(m.multiblockRenderID) match {
            case Some(r) =>
              return new AxisAlignedBB(getPos, getPos.add(r.boundingBox._1, r.boundingBox._2, r.boundingBox._3))
            case _ =>
          }
        case _ =>
      }
    }
    super.getRenderBoundingBox
  }

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    if (isController) {
      if (!isBuilding) {
        ticks = TileEntityUtils.incrementTicks(ticks, TileFrame.TICKS_TO_CHECK)
        if (ticks == 0)
          checkForRequiredItems()
      }
      else {
        progress += 1
        if (progress >= totalMachineBuildTime) {
          FrameMultiblockRegistry.getMultiblock(multiBlock) match {
            case Some(multi) =>
              TileFrame.shouldDrop = false
              TileFrame.shouldFullyRemove = false
              multi.formAtLocation(getLoc)
              TileFrame.shouldFullyRemove = true
              TileFrame.shouldDrop = true
            case _ =>
          }
        }
      }
    }
  }

  def checkForRequiredItems(): Unit = {
    FrameMultiblockRegistry.getMultiblock(multiBlock) match {
      case Some(multi) =>
        val items  = multi.getRequiredResources
        val random = new Random
        if (StorageUtils.removeItemsFromStorage(state.get.get.storage, items, false)) {
          state.get.foreach(_.storage.foreach { item =>
            if (!world.isRemote) InventoryUtils.dropItem(item, getLoc, random)
          })
          isBuilding = true
          setUpdate()
        }
      case _ =>
    }
  }

  override def clientUpdate(): Unit = {
    super.clientUpdate()
    if (isController) {
      if (isBuilding) {
        renderProgress += 1
      }
    }
  }

  def isCurrentlyBuilding: Boolean = {
    if (isController) isBuilding
    else if (isValidMultiBlock) {
      info.cLoc.getITileEntity() match {
        case Some(i: TileFrame) if i.isController => i.isBuilding
        case _ => false
      }
    }
    else false
  }

  def getRenderMark(i: Int, j: Int, k: Int) = TileFrame.getRenderMark(i, j, k, renderInt)

  def setRenderMark(bool: Boolean, i: Int, j: Int, k: Int): Unit = {
    renderInt = TileFrame.setRenderMark(bool, i, j, k, renderInt)
  }

  override def hasDescription: Boolean = isValidMultiBlock


  override def onBlockBreak(): Unit = {
    if (getWorld.isRemote) return

    if (TileFrame.shouldFullyRemove) {
      if (isController) {
        val random = new Random
        FrameMultiblockRegistry.getMultiblock(multiBlock) match {
          case Some(multi) =>
            multi.getTakenLocations(getLoc).foreach { loc =>
              getWorld.setBlockToAir(loc.getPos)
              if (TileFrame.shouldDrop) {
                val itemStack = Converter.IItemStackFromItemStack(new ItemStack(FemtoItems.itemFrame))
                ItemFrame.setSelection(itemStack, multiBlock)
                InventoryUtils.dropItem(itemStack, loc, random)
              }
            }
            if (isBuilding && TileFrame.shouldDrop)
              multi.getRequiredResources.foreach(i => InventoryUtils.dropItem(i, getLoc, random))
          case _ =>
        }
        state.get.foreach(_.storage.foreach(i => InventoryUtils.dropItem(i, getLoc, random)))
      }
      else info.cLoc.getITileEntity() match {
        case Some(_: TileFrame) => world.setBlockToAir(info.cLoc.getPos)
        case _ =>
      }
    }
  }


  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: EnumFacing): Boolean = {
    if (hasGUI) {
      info.cLoc.getITileEntity() match {
        case Some(tile: TileFrame) =>
          par5EntityPlayer.openGui(tile.getMod, tile.getGuiID, tile.getWorld, tile.getPos.getX, tile.getPos.getY, tile.getPos.getZ)
        case _ =>
      }
      true
    }
    else false
  }

  override def getMod: AnyRef = Femtocraft

  override def hasGUI: Boolean = isValidMultiBlock

  override def getGuiID: Int = if (isBuilding) GuiIDs.TileFrameConstructingGuiID else GuiIDs.TileFrameMultiblockGuiID
}
 */

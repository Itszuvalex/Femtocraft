package com.itszuvalex.femtocraft.industry.tile

import java.util.Random

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.item.ItemFrame
import com.itszuvalex.femtocraft.industry.{FrameMultiblockRegistry, FrameMultiblockRendererRegistry}
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.femtocraft.util.TileEntityUtils
import com.itszuvalex.femtocraft.{FemtoItems, Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.util.Comparators.ItemStack._
import com.itszuvalex.itszulib.util.InventoryUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.common.capabilities.Capability

import scala.collection.mutable

/**
  * Created by Christopher on 8/29/2015.
  */
object TileFrame {
  val BUILDING_KEY        = "Building"
  val RENDER_SETTINGS_KEY = "RenderSettings"
  val MULTIBLOCK_KEY      = "Multiblock"
  val PROGRESS_KEY        = "BuildProgress"

  var shouldDrop        = true
  var shouldFullyRemove = true

  val TICKS_TO_CHECK = 40

  def fullRender(bool: Boolean) = setRenderMarks(bool, 0, 0 until 20: _*)

  def fullRenderIndexes = (0 until 20).map(markIndexes)

  def markIndexes(mark: Int) = (mark / 12, (mark / 4) % 3, mark % 4)

  def getRenderMark(i: Int, j: Int, k: Int, renderInt: Int) = (renderInt & (1 << TileFrame.getSaveableIndentation(i, j, k))) > 0

  def getSaveableIndentation(i: Int, j: Int, k: Int) = {
    19 - (12 * i) - (4 * j) - k
  }

  def setRenderMark(bool: Boolean, i: Int, j: Int, k: Int, marker: Int): Int = {
    val num = marker
    val i1 = 1 << getSaveableIndentation(i, j, k)
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
      case (0, 0, 0) => setRenderMarks(true, 0, 4, 8, 11, 12, 16, 17, 19)
      case (`MaxX`, 0, 0) => setRenderMarks(true, 0, 5, 8, 9, 13, 16, 17, 18)
      case (0, `MaxY`, 0) => setRenderMarks(true, 0, 0, 3, 4, 12, 13, 15, 16)
      case (0, 0, `MaxZ`) => setRenderMarks(true, 0, 7, 10, 11, 15, 16, 18, 19)
      case (`MaxX`, `MaxY`, 0) => setRenderMarks(true, 0, 0, 1, 5, 12, 13, 14, 17)
      case (0, `MaxY`, `MaxZ`) => setRenderMarks(true, 0, 2, 3, 7, 12, 14, 15, 19)
      case (`MaxX`, 0, `MaxZ`) => setRenderMarks(true, 0, 6, 9, 10, 14, 17, 18, 19)
      case (`MaxX`, `MaxY`, `MaxZ`) => setRenderMarks(true, 0, 1, 2, 6, 13, 14, 15, 18)
      case (0, 0, _) => setRenderMarks(true, 0, 11, 16, 19)
      case (0, `MaxY`, _) => setRenderMarks(true, 0, 3, 12, 15)
      case (`MaxX`, 0, _) => setRenderMarks(true, 0, 9, 17, 18)
      case (`MaxX`, `MaxY`, _) => setRenderMarks(true, 0, 1, 13, 14)
      case (0, _, 0) => setRenderMarks(true, 0, 4, 12, 16)
      case (0, _, `MaxZ`) => setRenderMarks(true, 0, 7, 15, 19)
      case (`MaxX`, _, 0) => setRenderMarks(true, 0, 5, 13, 17)
      case (`MaxX`, _, `MaxZ`) => setRenderMarks(true, 0, 6, 14, 18)
      case (_, 0, 0) => setRenderMarks(true, 0, 8, 16, 17)
      case (_, 0, `MaxZ`) => setRenderMarks(true, 0, 10, 18, 19)
      case (_, `MaxY`, 0) => setRenderMarks(true, 0, 0, 12, 13)
      case (_, `MaxY`, `MaxZ`) => setRenderMarks(true, 0, 2, 14, 15)
      case _ => fullRender(false)
    }
  }

}

class TileFrame() extends TileEntityBase with MultiBlockComponent with TileMultiblockIndexedInventory with IInventory {
  var renderInt                                       = TileFrame.fullRender(true)
  var multiBlock           : String                   = null
  var renderProgress       : Int                      = 0
  var progress             : Int                      = 0
  // # of seconds * 20tps
  var totalMachineBuildTime: Int                      = 10 * 20
  var inProgressData       : mutable.Map[String, Any] = mutable.Map()
  var isBuilding           : Boolean                  = false
  var ticks                                           = 0

  var isModifyingInv: Boolean = false

  def calculateRendering(sizeX: Int, sizeY: Int, sizeZ: Int, locX: Int, locY: Int, locZ: Int) = {
    renderInt = TileFrame.renderPieces(sizeX, sizeY, sizeZ, locX, locY, locZ)
  }

  def calculateRendering(connectedDirs: Array[EnumFacing]): Unit = {
    renderInt = TileFrame.fullRender(true)
  }


  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability match {
    case c if capability == Capabilities.TILE_MULTIBLOCK => true
    case _ => super.hasCapability(capability, facing)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = capability match {
    case c if capability == Capabilities.TILE_MULTIBLOCK => info.asInstanceOf[T]
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
      info.cLoc.getTileEntity() match {
        case Some(i: TileFrame) if i.isController => i.isBuilding
        case _ => false
      }
    }
    else false
  }

  def checkForRequiredItems(): Unit = {
    FrameMultiblockRegistry.getMultiblock(multiBlock) match {
      case Some(multi) =>
        val items = multi.getRequiredResources
        //TODO: extract to generic "InventoryContainsItems" method in a util class somewhere.
        val itemsAndSlots = items.view.zip(items.map(getSlotsByItemStack(_).getOrElse(Set[Int]()))).map { case (item, slots) =>
          (item, slots.filter { slot =>
            getStackInSlot(slot) match {
              case null => false
              case i if i.isEmpty => false
              case i if IDDamageWildCardNBTComparator.compare(item, i) == 0 => true
              case _ => false
            }
          })
        }
        if (itemsAndSlots.
          forall { case (item, slots) =>
            if (slots.isEmpty) false
            else {
              var needed = item.getCount
              slots.exists { slot =>
                val i = getStackInSlot(slot)
                needed -= i.getCount
                needed <= 0
              }
            }
          }) {
          itemsAndSlots.foreach { case (item, slots) =>
            var needed = item.getCount
            slots.exists { slot =>
              val i = getStackInSlot(slot)
              val amt = Math.min(needed, i.getCount)
              needed -= amt
              i.setCount(i.getCount - amt)
              if (i.getCount <= 0) {
                isModifyingInv = true
                setInventorySlotContents(slot, ItemStack.EMPTY)
                isModifyingInv = false
              }
              needed <= 0
            }
          }
          isModifyingInv = true
          val random = new Random()
          indInventory.getInventory.zipWithIndex.foreach { case (item, slot) =>
            if (!world.isRemote) InventoryUtils.dropItem(Converter.IItemStackFromItemStack(item), getLoc, random)
            indInventory.setInventorySlotContents(slot, ItemStack.EMPTY)
          }
          isModifyingInv = false
          isBuilding = true
          setUpdate()
        }
      case _ =>
    }
  }

  def getRenderMark(i: Int, j: Int, k: Int) = TileFrame.getRenderMark(i, j, k, renderInt)

  def setRenderMark(bool: Boolean, i: Int, j: Int, k: Int): Unit = {
    renderInt = TileFrame.setRenderMark(bool, i, j, k, renderInt)
  }

  override def hasDescription: Boolean = isValidMultiBlock

  override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(compound)
    saveFrameInfo(compound)
    compound
  }

  def saveFrameInfo(compound: NBTTagCompound): Unit = {
    compound.setInteger(TileFrame.RENDER_SETTINGS_KEY, renderInt)
    compound.setString(TileFrame.MULTIBLOCK_KEY, if (multiBlock != null) multiBlock else "")
    compound.setInteger(TileFrame.PROGRESS_KEY, progress)
    compound.setBoolean(TileFrame.BUILDING_KEY, isBuilding)
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    loadFrameInfo(compound)
  }

  def loadFrameInfo(compound: NBTTagCompound): Unit = {
    renderInt = compound.Int(TileFrame.RENDER_SETTINGS_KEY)
    multiBlock = compound.String(TileFrame.MULTIBLOCK_KEY)
    if (multiBlock == "") multiBlock = null
    renderProgress = compound.Int(TileFrame.PROGRESS_KEY)
    isBuilding = compound.Bool(TileFrame.BUILDING_KEY)
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    saveFrameInfo(compound)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    loadFrameInfo(compound)
    setRenderUpdate()
  }

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
                val itemStack = new ItemStack(FemtoItems.itemFrame)
                ItemFrame.setSelection(itemStack, multiBlock)
                InventoryUtils.dropItem(Converter.IItemStackFromItemStack(itemStack), loc, random)
              }
            }
            if (isBuilding && TileFrame.shouldDrop)
              multi.getRequiredResources.foreach(i => InventoryUtils.dropItem(Converter.IItemStackFromItemStack(i), getLoc, random))
          case _ =>
        }
        indInventory.getInventory.foreach(i => InventoryUtils.dropItem(Converter.IItemStackFromItemStack(i), getLoc, random))
      }
      else info.cLoc.getTileEntity() match {
        case Some(frame: TileFrame) => world.setBlockToAir(info.cLoc.getPos)
        case _ =>
      }
    }
  }


  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: EnumFacing): Boolean = {
    if (hasGUI) {
      info.cLoc.getTileEntity() match {
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

  override def defaultInventory: IndexedInventory = new IndexedInventory(9)

  override def decrStackSize(slot: Int, amt: Int): ItemStack =
    if (isController) {
      val ret = indInventory.decrStackSize(slot, amt)
      markDirty()
      ret
    } else forwardToController[TileFrame, ItemStack](_.decrStackSize(slot, amt))


  override def closeInventory(player: EntityPlayer): Unit =
    if (isController) indInventory.closeInventory(player) else forwardToController[TileFrame, Unit](_.closeInventory(player))

  override def getSizeInventory: Int =
    if (isController) indInventory.getSizeInventory else forwardToController[TileFrame, Int](_.getSizeInventory)

  override def getInventoryStackLimit: Int =
    if (isController) indInventory.getInventoryStackLimit else forwardToController[TileFrame, Int](_.getInventoryStackLimit)

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean =
    if (isController) {
      if (isBuilding) false
      else indInventory.isItemValidForSlot(slot, item)
    } else forwardToController[TileFrame, Boolean](_.isItemValidForSlot(slot, item))


  override def openInventory(player: EntityPlayer): Unit =
    if (isController) indInventory.openInventory(player) else forwardToController[TileFrame, Unit](_.openInventory(player))

  override def setInventorySlotContents(slot: Int, item: ItemStack): Unit =
    if (isController) {
      indInventory.setInventorySlotContents(slot, item)
      markDirty()
    } else forwardToController[TileFrame, Unit](_.setInventorySlotContents(slot, item))

  override def isUsableByPlayer(player: EntityPlayer): Boolean =
    if (isController) indInventory.isUsableByPlayer(player) else forwardToController[TileFrame, Boolean](_.isUsableByPlayer(player))

  override def getStackInSlot(slot: Int): ItemStack =
    if (isController) indInventory.getStackInSlot(slot) else forwardToController[TileFrame, ItemStack](_.getStackInSlot(slot))

  override def hasCustomName: Boolean =
    if (isController) indInventory.hasCustomName else forwardToController[TileFrame, Boolean](_.hasCustomName)

  override def getName: String =
    if (isController) indInventory.getName else forwardToController[TileFrame, String](_.getName)

  override def markDirty(): Unit = {
    super.markDirty()
    if (!isModifyingInv)
      checkForRequiredItems()
  }

  override def clear(): Unit =
    if (isController) indInventory.clear() else forwardToController[TileFrame](_.clear())

  override def getFieldCount: Int =
    if (isController) 0 else forwardToController[TileFrame, Int](_.getFieldCount)

  override def getField(id: Int): Int =
    if (isController) 0 else forwardToController[TileFrame, Int](_.getField(id))

  override def removeStackFromSlot(index: Int): ItemStack =
    if (isController) {
      val ret = indInventory.getStackInSlot(index)
      indInventory.setInventorySlotContents(index, null)
      ret
    } else forwardToController[TileFrame, ItemStack](_.removeStackFromSlot(index))

  override def setField(id: Int, value: Int): Unit =
    if (isController) {} else forwardToController[TileFrame](_.setField(id, value))

  override def isEmpty: Boolean = indInventory.isEmpty
}

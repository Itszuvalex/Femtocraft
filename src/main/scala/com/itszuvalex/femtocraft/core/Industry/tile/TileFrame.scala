package com.itszuvalex.femtocraft.core.Industry.tile

import java.util.Random
import com.itszuvalex.femtocraft.core.Industry.{FrameMultiblockRendererRegistry, FrameMultiblockRegistry}
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.femtocraft.{FemtoItems, Femtocraft, GuiIDs, Resources}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import com.itszuvalex.itszulib.util.InventoryUtils
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{AxisAlignedBB, ResourceLocation}
import net.minecraftforge.common.util.ForgeDirection

import scala.collection.mutable

/**
 * Created by Christopher on 8/29/2015.
 */
object TileFrame {
  val RENDER_SETTINGS_KEY = "RenderSettings"
  val MULTIBLOCK_KEY      = "Multiblock"
  val PROGRESS_KEY        = "BuildProgress"

  var shouldDrop        = true
  var shouldFullyRemove = true

  def fullRender(bool: Boolean) = setRenderMarks(bool, 0, 0 until 20: _*)

  def fullRenderIndexes = (0 until 20).map(markIndexes)

  def markIndexes(mark: Int) = (mark / 12, (mark / 4) % 3, mark % 4)

  def getRenderMark(i: Int, j: Int, k: Int, renderInt: Int) = (renderInt & (1 << TileFrame.getSaveableIndentation(i, j, k))) > 0

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
      case (0, 0, 0)                => setRenderMarks(true, 0, 4, 8, 11, 12, 16, 17, 19)
      case (`MaxX`, 0, 0)           => setRenderMarks(true, 0, 5, 8, 9, 13, 16, 17, 18)
      case (0, `MaxY`, 0)           => setRenderMarks(true, 0, 0, 3, 4, 12, 13, 15, 16)
      case (0, 0, `MaxZ`)           => setRenderMarks(true, 0, 7, 10, 11, 15, 16, 18, 19)
      case (`MaxX`, `MaxY`, 0)      => setRenderMarks(true, 0, 0, 1, 5, 12, 13, 14, 17)
      case (0, `MaxY`, `MaxZ`)      => setRenderMarks(true, 0, 2, 3, 7, 12, 14, 15, 19)
      case (`MaxX`, 0, `MaxZ`)      => setRenderMarks(true, 0, 6, 9, 10, 14, 17, 18, 19)
      case (`MaxX`, `MaxY`, `MaxZ`) => setRenderMarks(true, 0, 1, 2, 6, 13, 14, 15, 18)
      case (0, 0, _)                => setRenderMarks(true, 0, 11, 16, 19)
      case (0, `MaxY`, _)           => setRenderMarks(true, 0, 3, 12, 15)
      case (`MaxX`, 0, _)           => setRenderMarks(true, 0, 9, 17, 18)
      case (`MaxX`, `MaxY`, _)      => setRenderMarks(true, 0, 1, 13, 14)
      case (0, _, 0)                => setRenderMarks(true, 0, 4, 12, 16)
      case (0, _, `MaxZ`)           => setRenderMarks(true, 0, 7, 15, 19)
      case (`MaxX`, _, 0)           => setRenderMarks(true, 0, 5, 13, 17)
      case (`MaxX`, _, `MaxZ`)      => setRenderMarks(true, 0, 6, 14, 18)
      case (_, 0, 0)                => setRenderMarks(true, 0, 8, 16, 17)
      case (_, 0, `MaxZ`)           => setRenderMarks(true, 0, 10, 18, 19)
      case (_, `MaxY`, 0)           => setRenderMarks(true, 0, 0, 12, 13)
      case (_, `MaxY`, `MaxZ`)      => setRenderMarks(true, 0, 2, 14, 15)
      case _                        => fullRender(false)
    }
  }

  def getSaveableIndentation(i: Int, j: Int, k: Int) = {
    19 - (12 * i) - (4 * j) - k
  }

}

class TileFrame() extends TileEntityBase with MultiBlockComponent with TileMultiblockIndexedInventory with IInventory {
  var renderInt                                               = TileFrame.fullRender(true)
  var multiBlock                   : String                   = null
  var progress                     : Int                      = 0
  var totalMachineBuildTime        : Float                    = 500f
  var inProgressData               : mutable.Map[String, Any] = mutable.Map()

  def calculateRendering(sizeX: Int, sizeY: Int, sizeZ: Int, locX: Int, locY: Int, locZ: Int) = {
    renderInt = TileFrame.renderPieces(sizeX, sizeY, sizeZ, locX, locY, locZ)
  }

  def calculateRendering(connectedDirs: Array[ForgeDirection]): Unit = {

  }


  override def getRenderBoundingBox: AxisAlignedBB = {
    if (isController) {
      FrameMultiblockRegistry.getMultiblock(multiBlock) match {
        case Some(m) =>
          FrameMultiblockRendererRegistry.getRenderer(m.multiblockRenderID) match {
            case Some(r) =>
              return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + r.boundingBox._1, yCoord + r.boundingBox._2, zCoord + r.boundingBox._3)
            case _ =>
          }
        case _ =>
      }
    }
    super.getRenderBoundingBox
  }

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    if (!isController) return
    if (getWorldObj.getTotalWorldTime % (totalMachineBuildTime / 100) == 0 && progress < 100) {
      progress += 1
      setUpdate()
    }
    if (progress >= 100 && worldObj.getTotalWorldTime >= inProgressData.getOrElse("targetTime", 0f).asInstanceOf[Float]) {
      FrameMultiblockRegistry.getMultiblock(multiBlock) match {
        case Some(multi) =>
          TileFrame.shouldDrop = false
          TileFrame.shouldFullyRemove = false
          multi.formAtLocation(getWorldObj, xCoord, yCoord, zCoord)
          TileFrame.shouldFullyRemove = true
          TileFrame.shouldDrop = true
        case _           =>
      }
    }
  }

  def getRenderMark(i: Int, j: Int, k: Int) = TileFrame.getRenderMark(i, j, k, renderInt)

  def setRenderMark(bool: Boolean, i: Int, j: Int, k: Int): Unit = {
    renderInt = TileFrame.setRenderMark(bool, i, j, k, renderInt)
  }

  override def getMod: AnyRef = Femtocraft

  override def hasDescription: Boolean = isValidMultiBlock

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    compound.setInteger(TileFrame.RENDER_SETTINGS_KEY, renderInt)
    compound.setString(TileFrame.MULTIBLOCK_KEY, if (multiBlock != null) multiBlock else "")
    compound.setInteger(TileFrame.PROGRESS_KEY, progress)
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    renderInt = compound.getInteger(TileFrame.RENDER_SETTINGS_KEY)
    multiBlock = compound.getString(TileFrame.MULTIBLOCK_KEY)
    if (multiBlock == "") multiBlock = null
    progress = compound.getInteger(TileFrame.PROGRESS_KEY)
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    compound.setInteger(TileFrame.RENDER_SETTINGS_KEY, renderInt)
    compound.setString(TileFrame.MULTIBLOCK_KEY, if (multiBlock != null) multiBlock else "")
    compound.setInteger(TileFrame.PROGRESS_KEY, progress)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    renderInt = compound.getInteger(TileFrame.RENDER_SETTINGS_KEY)
    multiBlock = compound.getString(TileFrame.MULTIBLOCK_KEY)
    if (multiBlock == "") multiBlock = null
    progress = compound.getInteger(TileFrame.PROGRESS_KEY)
    setRenderUpdate()
  }


  def onBlockBreak(): Unit = {
    if (getWorldObj.isRemote) return

    if (TileFrame.shouldFullyRemove) {
      if (isController) {
        val random = new Random
        FrameMultiblockRegistry.getMultiblock(multiBlock) match {
          case Some(multi) =>
            multi.getTakenLocations(getWorldObj, info.x, info.y, info.z).foreach { loc =>
              getWorldObj.setBlockToAir(loc.x, loc.y, loc.z)
              if (TileFrame.shouldDrop) {
                InventoryUtils.dropItem(new ItemStack(FemtoItems.itemFrame), getWorldObj, loc.x, loc.y, loc.z, random)
              }
                                                                                 }
          case _           =>
        }
        indInventory.getInventory.foreach {InventoryUtils.dropItem(_, getWorldObj, xCoord, yCoord, zCoord, random)}
      }
      else getWorldObj.getTileEntity(info.x, info.y, info.z) match {
        case frame: TileFrame => getWorldObj.setBlockToAir(info.x, info.y, info.z)
        case _                =>
      }
    }
  }


  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: Int): Boolean = {
    if (hasGUI) {
      par5EntityPlayer.openGui(getMod, getGuiID, worldObj, info.x, info.y, info.z)
      true
    }
    else false
  }

  override def hasGUI: Boolean = isValidMultiBlock

  override def getGuiID: Int = GuiIDs.FrameMultiblockGuiID

  override def defaultInventory: IndexedInventory = new IndexedInventory(9)

  override def decrStackSize(slot: Int, amt: Int): ItemStack =
    if (isController) indInventory.decrStackSize(slot, amt) else forwardToController[TileFrame, ItemStack](_.decrStackSize(slot, amt))

  override def closeInventory(): Unit =
    if (isController) indInventory.closeInventory() else forwardToController[TileFrame, Unit](_.closeInventory())

  override def getSizeInventory: Int =
    if (isController) indInventory.getSizeInventory else forwardToController[TileFrame, Int](_.getSizeInventory)

  override def getInventoryStackLimit: Int =
    if (isController) indInventory.getInventoryStackLimit else forwardToController[TileFrame, Int](_.getInventoryStackLimit)

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean =
    if (isController) indInventory.isItemValidForSlot(slot, item) else forwardToController[TileFrame, Boolean](_.isItemValidForSlot(slot, item))

  override def getStackInSlotOnClosing(slot: Int): ItemStack =
    if (isController) indInventory.getStackInSlotOnClosing(slot) else forwardToController[TileFrame, ItemStack](_.getStackInSlotOnClosing(slot))

  override def openInventory(): Unit =
    if (isController) indInventory.openInventory() else forwardToController[TileFrame, Unit](_.openInventory())

  override def setInventorySlotContents(slot: Int, item: ItemStack): Unit =
    if (isController) indInventory.setInventorySlotContents(slot, item) else forwardToController[TileFrame, Unit](_.setInventorySlotContents(slot, item))

  override def isUseableByPlayer(player: EntityPlayer): Boolean =
    if (isController) indInventory.isUseableByPlayer(player) else forwardToController[TileFrame, Boolean](_.isUseableByPlayer(player))

  override def getStackInSlot(slot: Int): ItemStack =
    if (isController) indInventory.getStackInSlot(slot) else forwardToController[TileFrame, ItemStack](_.getStackInSlot(slot))

  override def hasCustomInventoryName: Boolean =
    if (isController) indInventory.hasCustomInventoryName else forwardToController[TileFrame, Boolean](_.hasCustomInventoryName)

  override def getInventoryName: String =
    if (isController) indInventory.getInventoryName else forwardToController[TileFrame, String](_.getInventoryName)
}

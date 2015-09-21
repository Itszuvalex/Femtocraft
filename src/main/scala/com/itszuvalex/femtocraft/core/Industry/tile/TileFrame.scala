package com.itszuvalex.femtocraft.core.Industry.tile

import java.util.Random

import com.itszuvalex.femtocraft.core.FrameMultiblockRegistry
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.femtocraft.{FemtoItems, Femtocraft, GuiIDs, Resources}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import com.itszuvalex.itszulib.util.InventoryUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.util.ForgeDirection

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
  var renderInt                                       = TileFrame.fullRender(true)
  var multiBlock                   : String           = null
  var progress                     : Int              = 0
  var inProgressCurrentRenderedPart: Int              = 0
  var inProgressModelLoc           : ResourceLocation = null
  var inProgressTexLoc             : ResourceLocation = null
  var inProgressPartDelay          : Int              = 100
  var inProgressNextTargetTime     : Float            = 0

  def calculateRendering(sizeX: Int, sizeY: Int, sizeZ: Int, locX: Int, locY: Int, locZ: Int) = {
    renderInt = TileFrame.renderPieces(sizeX, sizeY, sizeZ, locX, locY, locZ)
  }

  /**
   * Sets the correct variables for the frame renderer to render the in-progress machine model.
   * @param currentRenderedPart Number of the last (or currently) rendered part of the machine.
   * @param modelLoc Location of the model in the "_in-progress" folder. Example: "arc furnace/Arc Furnace In-Progress.obj"
   * @param texLoc Location of the texture in the "_in-progress" folder. Example: "arc furnace/Arc Furnace In-Progress.png"
   */
  def renderInProgressMachine(currentRenderedPart: Int, modelLoc: String, texLoc: String, partDelay: Int): Unit = {
    inProgressCurrentRenderedPart = currentRenderedPart
    if (inProgressModelLoc == null) inProgressModelLoc = Resources.Model("_in-progress/" + modelLoc)
    if (inProgressTexLoc == null) inProgressTexLoc = Resources.Model("_in-progress/" + texLoc)
    inProgressPartDelay = partDelay
  }

  def calculateRendering(connectedDirs: Array[ForgeDirection]): Unit = {

  }

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    if (!isController) return
    if (getWorldObj.getTotalWorldTime % 10 == 0 && progress < 100) {
      progress += 1
      setUpdate()
    }
    if (progress >= 100 && getWorldObj.getTotalWorldTime >= inProgressNextTargetTime) {
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

  override def clientUpdate(): Unit = {
    super.clientUpdate()
    if (!isController) return
    if (multiBlock == null) return
    val lastPart = inProgressCurrentRenderedPart
    renderInProgressMachine(math.ceil(progress / 10d).toInt, multiBlock.toLowerCase + "/" + multiBlock + " In-Progress.obj", multiBlock.toLowerCase + "/" + multiBlock + " In-Progress.png", 100)
    if (lastPart != inProgressCurrentRenderedPart) inProgressNextTargetTime = getWorldObj.getTotalWorldTime + inProgressPartDelay
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
        FrameMultiblockRegistry.getMultiblock(multiBlock) match {
          case Some(multi) =>
            multi.getTakenLocations(getWorldObj, info.x, info.y, info.z).foreach { loc =>
              getWorldObj.setBlockToAir(loc.x, loc.y, loc.z)
              if (TileFrame.shouldDrop) InventoryUtils.dropItem(new ItemStack(FemtoItems.itemFrame), getWorldObj, loc.x, loc.y, loc.z, new Random)
                                                                                 }
          case _           =>
        }
      }
      else getWorldObj.getTileEntity(info.x, info.y, info.z) match {
        case frame: TileFrame => getWorldObj.setBlockToAir(info.x, info.y, info.z)
        case _                =>
      }
    }
  }


  override def hasGUI: Boolean = isValidMultiBlock

  override def getGuiID: Int = GuiIDs.FrameMultiblockGuiID

  override def defaultInventory: IndexedInventory = new IndexedInventory(9)

  override def decrStackSize(p_70298_1_ : Int, p_70298_2_ : Int): ItemStack =
    if (isController) indInventory.decrStackSize(p_70298_1_, p_70298_2_) else forwardToDifferentController[ItemStack, TileFrame](_.decrStackSize(p_70298_1_, p_70298_2_))

  override def closeInventory(): Unit =
    if (isController) indInventory.closeInventory() else forwardToController(_.closeInventory())

  override def getSizeInventory: Int =
    if (isController) indInventory.getSizeInventory else forwardToController(_.getSizeInventory)

  override def getInventoryStackLimit: Int =
    if (isController) indInventory.getInventoryStackLimit else forwardToController(_.getInventoryStackLimit)

  override def isItemValidForSlot(p_94041_1_ : Int, p_94041_2_ : ItemStack): Boolean =
    if (isController) indInventory.isItemValidForSlot(p_94041_1_, p_94041_2_) else forwardToController(_.isItemValidForSlot(p_94041_1_, p_94041_2_))

  override def getStackInSlotOnClosing(p_70304_1_ : Int): ItemStack =
    if (isController) indInventory.getStackInSlotOnClosing(p_70304_1_) else forwardToController(_.getStackInSlotOnClosing(p_70304_1_))

  override def openInventory(): Unit =
    if (isController) indInventory.openInventory() else forwardToController(_.openInventory())

  override def setInventorySlotContents(p_70299_1_ : Int, p_70299_2_ : ItemStack): Unit =
    if (isController) indInventory.setInventorySlotContents(p_70299_1_, p_70299_2_) else forwardToController(_.setInventorySlotContents(p_70299_1_, p_70299_2_))

  override def isUseableByPlayer(p_70300_1_ : EntityPlayer): Boolean =
    if (isController) indInventory.isUseableByPlayer(p_70300_1_) else forwardToController(_.isUseableByPlayer(p_70300_1_))

  override def getStackInSlot(p_70301_1_ : Int): ItemStack =
    if (isController) indInventory.getStackInSlot(p_70301_1_) else forwardToController(_.getStackInSlot(p_70301_1_))

  override def hasCustomInventoryName: Boolean =
    if (isController) indInventory.hasCustomInventoryName else forwardToController(_.hasCustomInventoryName)

  override def getInventoryName: String =
    if (isController) indInventory.getInventoryName else forwardToController(_.getInventoryName)
}

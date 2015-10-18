package com.itszuvalex.femtocraft.core.Cyber.tile

import java.util.Random

import com.itszuvalex.femtocraft.cyber.particle.FXWaterSpray
import com.itszuvalex.femtocraft.{FemtoFluids, GuiIDs, FemtoBlocks, Femtocraft}
import com.itszuvalex.femtocraft.core.Cyber.item.ItemBaseSeed
import com.itszuvalex.femtocraft.core.Cyber.CyberMachineRegistry
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.{TileMultiFluidTank, MultiBlockComponent}
import com.itszuvalex.itszulib.render.{Point3D, Vector3}
import com.itszuvalex.itszulib.util.InventoryUtils
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagString, NBTTagCompound}
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._

import scala.collection.mutable

/**
 * Created by Alex on 27.09.2015.
 */
object TileCyberBase {
  val CURRENT_MACHINE_KEY = "CurrentMachine"
  val PROGRESS_KEY = "Progress"
  val FIRST_FREE_SLOT_KEY = "FirstFreeSlot"
  val MACHINES_KEY = "Machines"
  val SLOTS_KEY = "MachineSlots"
  val SIZE_KEY = "Size"
  val BASE_COMPOUND_KEY = "CyberBaseInfo"

  val baseHeightMap = Map(1 -> 1, 2 -> 1, 3 -> 2)
  val slotHeightMap = Map(1 -> 4, 2 -> 6, 3 -> 10)

  /**
   * @param x X coord of lower-north-west corner
   * @param y Y coord of lower-north-west-corner
   * @param z Z coord of lower-north-west corner
   * @param dim Dimension ID
   * @param xSize X Size fo the cube
   * @param ySize Y Size fo the cube
   * @param zSize Z Size fo the cube
   * @return Set[Loc4] that represents a cube of the passed size.
   */
  def getLocationCube(x: Int, y: Int, z: Int, dim: Int, xSize: Int, ySize: Int, zSize: Int): Set[Loc4] = {
    {
      for {
        bx <- 0 until xSize
        by <- 0 until ySize
        bz <- 0 until zSize
      } yield Loc4(x + bx, y + by, z + bz, dim)
    }.toSet
  }

  /**
   * @param size Size number of the machine
   * @param x X coord of lower-north-west corner
   * @param y Y coord of lower-north-west-corner
   * @param z Z coord of lower-north-west corner
   * @param dim Dimension id of the machine
   * @return Set of locations that are occupied by the base
   */
  def getBaseLocations(size: Int, x: Int, y: Int, z: Int, dim: Int): Set[Loc4] =
    getLocationCube(x, y, z, dim, size, baseHeightMap(size), size)

  /**
   * @param size Size number of the machine
   * @param x X coord of lower-north-west corner
   * @param y Y coord of lower-north-west-corner
   * @param z Z coord of lower-north-west corner
   * @param dim Dimension id of the machine
   * @return Set of locations that are occupied by the machine slots of the base
   */
  def getSlotLocations(size: Int, x: Int, y: Int, z: Int, dim: Int): Set[Loc4] =
    getLocationCube(x, y + baseHeightMap(size), z, dim, size, slotHeightMap(size), size)

  /**
   * @param locs Set of locations to check
   * @return True if all blocks at all locations in locs are air or replaceable, false otherwise
   */
  def areAllPlaceable(locs: Set[Loc4]) = locs.forall( loc => {val world = DimensionManager.getWorld(loc.dim); world.isAirBlock(loc.x, loc.y, loc.z) || world.getBlock(loc.x, loc.y, loc.z).isReplaceable(world, loc.x, loc.y, loc.z)} )

  /**
   * Used to check whether the first slot above a base is blocked, to deny construction of the base if it is.
   * @param locs Set of locations to check
   * @param y Y plane to check
   * @return True if all blocks of all locations in locs that have the given y coordinate are air or replaceable, false otherwise
   */
  def arePartsAtYPlaceable(locs: Set[Loc4], y: Int) = locs.forall( loc => {val world = DimensionManager.getWorld(loc.dim); world.isAirBlock(loc.x, loc.y, loc.z) || world.getBlock(loc.x, loc.y, loc.z).isReplaceable(world, loc.x, loc.y, loc.z) || loc.y != y} )
}

class TileCyberBase extends TileEntityBase with MultiBlockComponent with TileMultiblockIndexedInventory with TileMultiFluidTank with IInventory {
  var size: Int                                               = 1
  var machines: Array[String]                                 = new Array[String](10)
  var machineSlotMap: Array[Int]                              = new Array[Int](10)
  var firstFreeSlot: Int                                      = 0
  var currentlyBuildingMachine: Int                           = -1
  var currentMachineBuildProgress: Int                        = 0
  var totalMachineBuildTime: Float                            = 100f
  var inProgressData: mutable.Map[String, Any]                = mutable.Map.empty[String, Any]
  private var breaking: Boolean                               = false

  override def onSideActivate(player: EntityPlayer, side: Int): Boolean = {
    if (worldObj.isRemote) return true
    if (isValidMultiBlock) {
      if (!player.isSneaking) {
        if (hasGUI) player.openGui(getMod, getGuiID, worldObj, info.x, info.y, info.z)
        return true
      }
    }
    false
  }

  override def getRenderBoundingBox: AxisAlignedBB = {
    if (isController) {
      var yOffset = 0
      if (currentlyBuildingMachine == -1) {
        yOffset = TileCyberBase.baseHeightMap(size)
      } else {
        yOffset = TileCyberBase.baseHeightMap(size) + TileCyberBase.slotHeightMap(size)
      }
      return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + size, yCoord + yOffset, zCoord + size)
    }
    super.getRenderBoundingBox
  }

  def onBlockBreak(): Unit = {
    if (!isValidMultiBlock || worldObj.isRemote) return
    if (isController) {
      TileCyberBase.getSlotLocations(size, xCoord, yCoord, zCoord, worldObj.provider.dimensionId).foreach { loc =>
        if (loc.getBlock().orNull == FemtoBlocks.blockInProgressMachine) worldObj.setBlockToAir(loc.x, loc.y, loc.z)
      }
      breakMachinesUpwardsFromSlot(0)
      TileCyberBase.getBaseLocations(size, xCoord, yCoord, zCoord, worldObj.provider.dimensionId).foreach { loc =>
        worldObj.setBlockToAir(loc.x, loc.y, loc.z)
        worldObj.removeTileEntity(loc.x, loc.y, loc.z)
      }
      InventoryUtils.dropItem(ItemBaseSeed.createStack(1, size), worldObj, xCoord + (size / 2), yCoord, zCoord + (size / 2), new Random())
    } else {
      worldObj.getTileEntity(info.x, info.y, info.z) match {
        case te: TileCyberBase => worldObj.setBlockToAir(info.x, info.y, info.z)
        case _ =>
      }
    }
  }

  private def firstEmpty(ar: Array[String]): Int = {
    for(i <- 0 until ar.length) if (ar(i) == null) return i
    ar.length
  }

  private def getOrNullStr(ar: Array[String], index: Int): String = {
    if (ar(index) == null) return "null"
    ar(index)
  }

  private def setToStrOrNull(ar: Array[String], index: Int, value: String): Unit = {
    if (value == "null") ar(index) = null
    else ar(index) = value
  }

  def yFromSlot(slot: Int): Int = yCoord + TileCyberBase.baseHeightMap(size) + slot

  def remainingSlots: Int = {
    val max = TileCyberBase.slotHeightMap(size) - firstFreeSlot
    for (i <- 0 until max) {
      if (TileCyberBase.areAllPlaceable(TileCyberBase.getLocationCube(xCoord, yFromSlot(firstFreeSlot), zCoord, worldObj.provider.dimensionId, size, max - i, size))) return max - i
    }
    0
  }

  def buildMachine(name: String): Unit = {
    if (worldObj.isRemote) return
    if (!isController) forwardToController[TileCyberBase, Unit](_.buildMachine(name))
    if (currentlyBuildingMachine > -1) return
    CyberMachineRegistry.getMachine(name) match {
      case Some(m) =>
        if (remainingSlots < m.getRequiredSlots) return
        if (size != m.getRequiredBaseSize) return
        m.getTakenLocations(worldObj, xCoord, yFromSlot(firstFreeSlot), zCoord).foreach { loc =>
          worldObj.setBlock(loc.x, loc.y, loc.z, FemtoBlocks.blockInProgressMachine)
        }
      case _ => return
    }
    currentlyBuildingMachine = firstEmpty(machines)
    machines(currentlyBuildingMachine) = name
    machineSlotMap(currentlyBuildingMachine) = firstFreeSlot
  }

  def breakMachinesUpwardsFromSlot(slot: Int): Unit = {
    if (breaking) return
    breaking = true
    machineSlotMap.zipWithIndex.foreach { mac =>
      if (mac._1 >= slot) {
        CyberMachineRegistry.getMachine(machines(mac._2)) match {
          case Some(m) =>
            m.breakMachine(worldObj, xCoord, yFromSlot(mac._1), zCoord)
            machines(mac._2) = null
            machineSlotMap(mac._2) = 0
          case _ =>
        }
      }
    }
    firstFreeSlot = slot
    breaking = false
  }

  /**
   * Tries to put an item into the base's buffer inventory. If not all of it fits, the rest is returned.
   * @param item ItemStack to put into buffer inventory.
   * @return Remaining ItemStack, null if empty.
   */
  def putItem(item: ItemStack): ItemStack = {
    for (id <- 9 until indInventory.getSizeInventory) {
      val invStack = indInventory.getStackInSlot(id)
      if (item.isItemEqual(invStack) && ItemStack.areItemStackTagsEqual(item, invStack)) {
        val fitAmount = indInventory.getInventoryStackLimit - invStack.stackSize
        if (item.stackSize <= fitAmount) {
          indInventory.removeItemStack(invStack, id)
          invStack.stackSize += item.stackSize
          indInventory.addItemStack(invStack, id)
          item.stackSize = 0
        } else {
          indInventory.removeItemStack(invStack, id)
          invStack.stackSize = indInventory.getInventoryStackLimit
          indInventory.addItemStack(invStack, id)
          item.stackSize -= fitAmount
        }
      } else if (invStack == null) {
        indInventory.addItemStack(item, id)
        item.stackSize = 0
      }
    }
    if (item.stackSize == 0) null else item
  }

  /**
   * Tries to put an item into the base's buffer tanks. If not all of it fits, the rest is returned.
   * @param fluid FluidStack to put into buffer tanks.
   * @return Remaining FluidStack, null if empty.
   */
  def putFluid(fluid: FluidStack): FluidStack = {
    if (!isController) return forwardToController[TileCyberBase, FluidStack](_.putFluid(fluid))
    fluid.amount -= fill(ForgeDirection.DOWN, fluid, true)
    if (fluid.amount == 0) null else fluid
  }

  /**
   * Broadcasts an ItemStack to all machines on this base. If not all is accepted, the rest tries to go into the base's buffer inventory.
   * If it doesn't all fit in there, the rest is returned.
   * @param _item ItemStack to broadcast
   * @return Remaining ItemStack, null if empty.
   */
  def broadcastItem(_item: ItemStack): ItemStack = {
    if (!isController) return forwardToController[TileCyberBase, ItemStack](_.broadcastItem(_item))
    var item = _item
    for (i <- 0 until firstEmpty(machines)) {
      CyberMachineRegistry.getMachine(machines(i)) match {
        case Some(m) =>
          item = m.receiveItemBroadcast(item, worldObj, xCoord, yFromSlot(machineSlotMap(i)), zCoord)
          if (item.stackSize == 0) return null
        case _ =>
      }
    }
    putItem(item)
  }

  /**
   * Broadcasts a FluidStack to all machines on this base. If not all is accepted, the rest tries to go into the base's buffer tanks.
   * If it doesn't all fit in there, the rset is returned.
   * @param _fluid FluidStack to broadcast
   * @return Remaining FluidStack, null if empty.
   */
  def broadcastFluid(_fluid: FluidStack): FluidStack = {
    if (!isController) { return forwardToController[TileCyberBase, FluidStack](_.broadcastFluid(_fluid)) }
    var fluid = _fluid
    for (i <- 0 until firstEmpty(machines)) {
      CyberMachineRegistry.getMachine(machines(i)) match {
        case Some(m) =>
          fluid = m.receiveFluidBroadcast(fluid, worldObj, xCoord, yFromSlot(machineSlotMap(i)), zCoord)
          if (fluid.amount == 0) return null
        case _ =>
      }
    }
    putFluid(fluid)
  }

  override def serverUpdate(): Unit = {
    /* Test Code: fill(ForgeDirection.DOWN, new FluidStack(FluidRegistry.LAVA, 2), true)
                  setUpdateTanks() */
    super.serverUpdate()
    if (!isController || currentlyBuildingMachine == -1) return
    if (worldObj.getTotalWorldTime % (totalMachineBuildTime / 100) == 0 && currentMachineBuildProgress < 100) {
      currentMachineBuildProgress += 1
      setUpdate()
    }
    if (currentMachineBuildProgress >= 100 && worldObj.getTotalWorldTime >= inProgressData.getOrElse("targetTime", 0f).asInstanceOf[Float]) {
      CyberMachineRegistry.getMachine(machines(currentlyBuildingMachine)) match {
        case Some(m) =>
          m.formAtBaseAndIndex(worldObj, this, currentlyBuildingMachine)
          firstFreeSlot += m.getRequiredSlots
        case _ =>
      }
      currentlyBuildingMachine = -1
      currentMachineBuildProgress = 0
      inProgressData.clear()
    }
  }

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    val comp = new NBTTagCompound()
    comp.setInteger(TileCyberBase.SIZE_KEY, size)
    comp.setInteger(TileCyberBase.CURRENT_MACHINE_KEY, currentlyBuildingMachine)
    comp.setInteger(TileCyberBase.PROGRESS_KEY, currentMachineBuildProgress)
    comp.setInteger(TileCyberBase.FIRST_FREE_SLOT_KEY, firstFreeSlot)
    comp.setTag(TileCyberBase.MACHINES_KEY, NBTList(for(i <- 0 until machines.length) yield new NBTTagString(getOrNullStr(machines, i))))
    comp.setIntArray(TileCyberBase.SLOTS_KEY, machineSlotMap)
    compound.setTag(TileCyberBase.BASE_COMPOUND_KEY, comp)
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    val comp = compound.getCompoundTag(TileCyberBase.BASE_COMPOUND_KEY)
    size = comp.getInteger(TileCyberBase.SIZE_KEY)
    currentlyBuildingMachine = comp.getInteger(TileCyberBase.CURRENT_MACHINE_KEY)
    currentMachineBuildProgress = comp.getInteger(TileCyberBase.PROGRESS_KEY)
    firstFreeSlot = comp.getInteger(TileCyberBase.FIRST_FREE_SLOT_KEY)
    val machineList = comp.getTagList(TileCyberBase.MACHINES_KEY, 8)
    for (i <- 0 until machineList.tagCount()) setToStrOrNull(machines, i, machineList.getStringTagAt(i))
    machineSlotMap = comp.getIntArray(TileCyberBase.SLOTS_KEY)
    indInventory.setInventorySize(9 + math.pow(size + 1, 2).toInt)
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    val comp = new NBTTagCompound()
    comp.setInteger(TileCyberBase.SIZE_KEY, size)
    comp.setInteger(TileCyberBase.CURRENT_MACHINE_KEY, currentlyBuildingMachine)
    comp.setInteger(TileCyberBase.PROGRESS_KEY, currentMachineBuildProgress)
    comp.setInteger(TileCyberBase.FIRST_FREE_SLOT_KEY, firstFreeSlot)
    comp.setTag(TileCyberBase.MACHINES_KEY, NBTList(for(i <- 0 until machines.length) yield new NBTTagString(getOrNullStr(machines, i))))
    comp.setIntArray(TileCyberBase.SLOTS_KEY, machineSlotMap)
    compound.setTag(TileCyberBase.BASE_COMPOUND_KEY, comp)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    val comp = compound.getCompoundTag(TileCyberBase.BASE_COMPOUND_KEY)
    size = comp.getInteger(TileCyberBase.SIZE_KEY)
    currentlyBuildingMachine = comp.getInteger(TileCyberBase.CURRENT_MACHINE_KEY)
    currentMachineBuildProgress = comp.getInteger(TileCyberBase.PROGRESS_KEY)
    firstFreeSlot = comp.getInteger(TileCyberBase.FIRST_FREE_SLOT_KEY)
    val machineList = comp.getTagList(TileCyberBase.MACHINES_KEY, 8)
    for (i <- 0 until machineList.tagCount()) setToStrOrNull(machines, i, machineList.getStringTagAt(i))
    machineSlotMap = comp.getIntArray(TileCyberBase.SLOTS_KEY)
    indInventory.setInventorySize(9 + math.pow(size + 1, 2).toInt)
    setRenderUpdate()
  }

  override def hasGUI: Boolean = isValidMultiBlock

  override def getGuiID: Int = GuiIDs.CyberBaseGuiID

  override def getMod: AnyRef = Femtocraft

  override def decrStackSize(slot : Int, amt : Int): ItemStack = if (isController) indInventory.decrStackSize(slot, amt) else forwardToController[TileCyberBase, ItemStack](_.decrStackSize(slot, amt))

  override def closeInventory(): Unit = if (isController) indInventory.closeInventory() else forwardToController[TileCyberBase, Unit](_.closeInventory())

  override def getSizeInventory: Int = if (isController) indInventory.getSizeInventory else forwardToController[TileCyberBase, Int](_.getSizeInventory)

  override def getInventoryStackLimit: Int = if (isController) indInventory.getInventoryStackLimit else forwardToController[TileCyberBase, Int](_.getInventoryStackLimit)

  override def isItemValidForSlot(slot : Int, stack : ItemStack): Boolean = if (isController) indInventory.isItemValidForSlot(slot, stack) else forwardToController[TileCyberBase, Boolean](_.isItemValidForSlot(slot, stack))

  override def getStackInSlotOnClosing(slot : Int): ItemStack = if (isController) indInventory.getStackInSlotOnClosing(slot) else forwardToController[TileCyberBase, ItemStack](_.getStackInSlotOnClosing(slot))

  override def openInventory(): Unit = if (isController) indInventory.openInventory() else forwardToController[TileCyberBase, Unit](_.openInventory())

  override def setInventorySlotContents(slot : Int, stack : ItemStack): Unit = if (isController) indInventory.setInventorySlotContents(slot, stack) else forwardToController[TileCyberBase, Unit](_.setInventorySlotContents(slot, stack))

  override def isUseableByPlayer(player : EntityPlayer): Boolean = if (isController) indInventory.isUseableByPlayer(player) else forwardToController[TileCyberBase, Boolean](_.isUseableByPlayer(player))

  override def getStackInSlot(slot : Int): ItemStack = if (isController) indInventory.getStackInSlot(slot) else forwardToController[TileCyberBase, ItemStack](_.getStackInSlot(slot))

  override def hasCustomInventoryName: Boolean = if (isController) indInventory.hasCustomInventoryName else forwardToController[TileCyberBase, Boolean](_.hasCustomInventoryName)

  override def getInventoryName: String = if (isController) indInventory.getInventoryName else forwardToController[TileCyberBase, String](_.getInventoryName)

  override def defaultInventory: IndexedInventory = new IndexedInventory(13)

  override def hasDescription: Boolean = isValidMultiBlock

  override def defaultTanks: Array[FluidTank] = Array(new FluidTank(2000), new FluidTank(2000))

  // Disabled UP because blocks directly on top of the base (that aren't machines) are generally forbidden.
  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = from != ForgeDirection.UP

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = from != ForgeDirection.UP

  override def fill(from: ForgeDirection, fluid: FluidStack, doFill: Boolean): Int = {
    var filled = 0
    setUpdateTanks()
    if (fluid.getFluid == FemtoFluids.cybermass) {
      val filled2 = tanks(0).fill(fluid, doFill)
      fluid.amount -= filled2
      if (fluid.amount == 0) return filled2
      filled = filled2
    }
    filled += tanks(1).fill(fluid, doFill)
    fluid.amount -= filled
    if (size < 3 || fluid.amount == 0) return filled
    filled += tanks(2).fill(fluid, doFill)
    filled
  }

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack = {
    setUpdateTanks()
    if (size == 3) {
      val ret = tanks(2).drain(maxDrain, false)
      if (ret == null || ret.amount == 0) return tanks(1).drain(maxDrain, doDrain)
      return tanks(2).drain(maxDrain, doDrain)
    }
    tanks(1).drain(maxDrain, doDrain)
  }

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack = {
    var t2rf = false
    if (size == 3) {
      t2rf = tanks(2).getFluid.getFluid == resource.getFluid
    }
    val t1rf = tanks(1).getFluid.getFluid == resource.getFluid
    setUpdateTanks()
    if (t2rf) return tanks(2).drain(resource.amount, doDrain)
    if (t1rf) return tanks(1).drain(resource.amount, doDrain)
    null
  }
}

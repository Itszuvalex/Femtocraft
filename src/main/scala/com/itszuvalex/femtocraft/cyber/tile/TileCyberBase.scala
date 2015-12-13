package com.itszuvalex.femtocraft.cyber.tile

import java.util.Random

import com.itszuvalex.femtocraft.cyber.item.ItemBaseSeed
import com.itszuvalex.femtocraft.cyber.tile.TileCyberBase.MachineMapping
import com.itszuvalex.femtocraft.cyber.{CyberMachineRegistry, ICyberMachineMultiblock}
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.femtocraft.{FemtoBlocks, FemtoFluids, Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.{Loc4, NBTSerializable}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.{MultiBlockComponent, TileMultiFluidTank}
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import com.itszuvalex.itszulib.util.InventoryUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._

import scala.collection.mutable

/**
  * Created by Alex on 27.09.2015.
  */
object TileCyberBase {
  val MACHINES_KEY = "Machines"
  val COMPOUND_KEY = "CyberBase"
  val SIZE_KEY = "Size"
  val baseHeightMap = Map(1 -> 1, 2 -> 1, 3 -> 2)
  val slotHeightMap = Map(1 -> 4, 2 -> 6, 3 -> 10)

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
    * @return Set of locations that are occupied by the machine slots of the base
    */
  def getSlotLocations(size: Int, x: Int, y: Int, z: Int, dim: Int): Set[Loc4] =
    getLocationCube(x, y + baseHeightMap(size), z, dim, size, slotHeightMap(size), size)

  /**
    * @param locs Set of locations to check
    * @return True if all blocks at all locations in locs are air or replaceable, false otherwise
    */
  def areAllPlaceable(locs: Set[Loc4]) =
    locs.forall(loc => {
      val world = loc.getWorld.get
      world.isAirBlock(loc.x, loc.y, loc.z) || world.getBlock(loc.x, loc.y, loc.z).isReplaceable(world, loc.x, loc.y, loc.z)
    })

  /**
    * Used to check whether the first slot above a base is blocked, to deny construction of the base if it is.
    * @param locs Set of locations to check
    * @param y Y plane to check
    * @return True if all blocks of all locations in locs that have the given y coordinate are air or replaceable, false otherwise
    */
  def arePartsAtYPlaceable(locs: Set[Loc4], y: Int) =
    locs.forall(loc => {
      val world = loc.getWorld.get
      world.isAirBlock(loc.x, loc.y, loc.z) || world.getBlock(loc.x, loc.y, loc.z).isReplaceable(world, loc.x, loc.y, loc.z) || loc.y != y
    })

  private case class MachineMapping(var startingSlot: Int,
                                    var controllerLoc: Loc4) extends NBTSerializable with Ordered[MachineMapping] {
    def cyberMachine = {
      val name =
        controllerLoc.getTileEntity(true) match {
          case Some(a: ICyberMachineMultiblock) => a.getCyberMachine
          case _ => null
        }
      CyberMachineRegistry.getMachine(name)
    }

    override def saveToNBT(compound: NBTTagCompound): Unit = {
      compound("startingSlot" -> startingSlot,
        "controllerLoc" -> NBTCompound(controllerLoc))
    }

    override def loadFromNBT(compound: NBTTagCompound): Unit = {
      startingSlot = compound.Int("startingSlot")
      controllerLoc = compound.NBTCompound("controllerLoc")(Loc4(_))
    }

    override def compare(y: MachineMapping): Int = {
      (this, y) match {
        case (null, null) => 0
        case (_, null) => 1
        case (null, _) => -1
        case _ =>
          this.startingSlot.compareTo(y.startingSlot)
      }
    }

    private def this() = this(0, null)
  }

  private object MachineMapping {
    def apply(compound: NBTTagCompound) = {
      val mapping = new MachineMapping()
      mapping.loadFromNBT(compound)
      mapping
    }
  }

}

class TileCyberBase extends TileEntityBase with MultiBlockComponent with TileMultiblockIndexedInventory with TileMultiFluidTank with IInventory {
  var size: Int = 1
  //  var machineSlotMap             : Array[Int]               = new Array[Int](10) // Wtf is this here for?
  //  var currentlyBuildingMachine   : Int                      = -1 //No need to have the base track the building.
  //  var currentMachineBuildProgress: Int                      = 0  //The machine itself can do it.
  //  var totalMachineBuildTime      : Float                    = 100f
  var inProgressData: mutable.Map[String, Any] = mutable.Map.empty[String, Any]
  private var machinesList = mutable.TreeSet[MachineMapping]()
  //Though this is the pickle.
  private var breaking: Boolean = false

  def firstFreeSlot: Int = machinesList.lastOption.map(topSlotForMachine(_) + 1).getOrElse(0)

  override def onSideActivate(player: EntityPlayer, side: Int): Boolean = {
    if (isValidMultiBlock) {
      if (!player.isSneaking) {
        if (hasGUI) player.openGui(getMod, getGuiID, worldObj, info.x, info.y, info.z)
        return true
      }
    }
    false
  }

  override def hasGUI: Boolean = isValidMultiBlock

  override def getGuiID: Int = GuiIDs.CyberBaseGuiID

  override def getMod: AnyRef = Femtocraft

  override def getRenderBoundingBox: AxisAlignedBB = if (isController) {
    AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + size, yCoord + getBaseheight, zCoord + size)
  } else super.getRenderBoundingBox

  def getBaseheight: Int = {
    TileCyberBase.baseHeightMap(size)
  }

  def onBlockBreak(): Unit = {
    if (worldObj.isRemote) return
    if (isController) {
      //      TileCyberBase.getSlotLocations(size, xCoord, yCoord, zCoord, worldObj.provider.dimensionId).foreach { loc =>
      //        if (loc.getBlock().orNull == FemtoBlocks.blockInProgressMachine) worldObj.setBlockToAir(loc.x, loc.y, loc.z)
      //                                                                                                          }
      breakMachinesUpwardsFromSlot(0)
      TileCyberBase.getBaseLocations(size, xCoord, yCoord, zCoord, worldObj.provider.dimensionId).foreach { loc =>
        worldObj.setBlockToAir(loc.x, loc.y, loc.z)
      }
      InventoryUtils.dropItem(ItemBaseSeed.createStack(1, size), worldObj, xCoord + (size / 2), yCoord, zCoord + (size / 2), new Random())
    }
    else {
      worldObj.getTileEntity(info.x, info.y, info.z) match {
        case te: TileCyberBase => worldObj.setBlockToAir(info.x, info.y, info.z)
        case _ =>
      }
    }
  }

  def breakMachinesUpwardsFromSlot(slot: Int): Unit = {
    if (breaking) return
    breaking = true
    (slot until getNumSlots).flatMap(getMachine).toSet[MachineMapping]
      .foreach { m =>
        m.cyberMachine match {
          case Some(c) =>
            c.breakMachine(m.controllerLoc.getWorld.get, m.controllerLoc.x, m.controllerLoc.y, m.controllerLoc.z)
            machinesList.remove(m)
          case None =>
        }
      }
    breaking = false
  }

  private def getMachine(slot: Int): Option[MachineMapping] = {
    var ret: MachineMapping = null
    machinesList.foreach { machine =>
      machine.startingSlot match {
        case i if i < slot => ret = machine
        case i if i == slot => return Option(machine)
        case _ => return Option(ret)
      }
    }
    Option(ret)
  }

  def yFromSlot(slot: Int): Int = yCoord + getBaseheight + slot

  def remainingSlots = getNumSlots - machinesList.lastOption.map(topSlotForMachine(_) + 1).getOrElse(0)

  def getNumSlots = TileCyberBase.slotHeightMap(size)

  def buildMachine(name: String): Unit = {
    if (worldObj.isRemote) return
    if (!isController) {
      worldObj.getTileEntity(info.x, info.y, info.z) match {
        case base: TileCyberBase => base.buildMachine(name)
        case _ =>
      }
      return
    }
    CyberMachineRegistry.getMachine(name) match {
      case Some(m) =>
        if (size != m.getRequiredBaseSize) return
        if (remainingSlots < m.getRequiredSlots) return
        val freeSlot = firstFreeSlot
        val slotY = yFromSlot(freeSlot)
        val controllerLoc = Loc4(xCoord, slotY, zCoord, worldObj.provider.dimensionId)
        m.getTakenLocations(worldObj, controllerLoc.x, controllerLoc.y, controllerLoc.z).foreach { loc =>
          worldObj.setBlock(loc.x, loc.y, loc.z, FemtoBlocks.blockCyberMachineInProgress)
          worldObj.getTileEntity(loc.x, loc.y, loc.z) match {
            case cin: TileCyberMachineInProgress =>
              cin.machineInProgress = name
              cin.setIndexInBase(freeSlot)
              cin.setBasePos(getLoc)
              cin.formMultiBlock(worldObj, controllerLoc.x, controllerLoc.y, controllerLoc.z)
            case _ =>
          }
        }
        machinesList += MachineMapping(freeSlot, controllerLoc)
      case _ =>
    }
  }

  /**
    * Tries to put an item into the base's buffer inventory. If not all of it fits, the rest is returned.
    * @param item ItemStack to put into buffer inventory.
    * @return Remaining ItemStack, null if empty.
    */
  def putItem(item: ItemStack): ItemStack = {
    for (id <- 9 until indInventory.getSizeInventory) {
      val invStack = indInventory.getStackInSlot(id)
      if (invStack == null) {
        indInventory.addItemStack(item, id)
        item.stackSize = 0
      } else if (item.isItemEqual(invStack) && ItemStack.areItemStackTagsEqual(item, invStack)) {
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
    //    var item = _item
    //    for (i <- 0 until firstEmpty(machines)) {
    //      CyberMachineRegistry.getMachine(machines(i)) match {
    //        case Some(m) =>
    //          item = m.receiveItemBroadcast(item, worldObj, xCoord, yFromSlot(machineSlotMap(i)), zCoord)
    //          if (item.stackSize == 0) return null
    //        case _ =>
    //      }
    //    }
    //    putItem(item)
    null
  }

  /**
    * Broadcasts a FluidStack to all machines on this base. If not all is accepted, the rest tries to go into the base's buffer tanks.
    * If it doesn't all fit in there, the rset is returned.
    * @param _fluid FluidStack to broadcast
    * @return Remaining FluidStack, null if empty.
    */
  def broadcastFluid(_fluid: FluidStack): FluidStack = {
    if (!isController) {
      return forwardToController[TileCyberBase, FluidStack](_.broadcastFluid(_fluid))
    }
    //    var fluid = _fluid
    //    for (i <- 0 until firstEmpty(machines)) {
    //      CyberMachineRegistry.getMachine(machines(i)) match {
    //        case Some(m) =>
    //          fluid = m.receiveFluidBroadcast(fluid, worldObj, xCoord, yFromSlot(machineSlotMap(i)), zCoord)
    //          if (fluid.amount == 0) return null
    //        case _ =>
    //      }
    //    }
    //    putFluid(fluid)
    null
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    val comp = new NBTTagCompound()
    comp.setInteger(TileCyberBase.SIZE_KEY, size)
    compound.setTag(TileCyberBase.COMPOUND_KEY, comp)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    val comp = compound.getCompoundTag(TileCyberBase.COMPOUND_KEY)
    size = comp.getInteger(TileCyberBase.SIZE_KEY)
    setRenderUpdate()
  }

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    compound(TileCyberBase.COMPOUND_KEY ->
      NBTCompound(
        TileCyberBase.MACHINES_KEY -> NBTList(machinesList.map(NBTCompound)),
        TileCyberBase.SIZE_KEY -> size)
    )
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    compound.NBTCompound(TileCyberBase.COMPOUND_KEY) { comp =>
      machinesList.clear()
      machinesList ++= comp.NBTList(TileCyberBase.MACHINES_KEY).map(MachineMapping(_))
      size = comp.Int(TileCyberBase.SIZE_KEY)
      Unit
    }
  }

  override def decrStackSize(slot: Int, amt: Int): ItemStack = if (isController) indInventory.decrStackSize(slot, amt) else forwardToController[TileCyberBase, ItemStack](_.decrStackSize(slot, amt))

  override def closeInventory(): Unit = if (isController) indInventory.closeInventory() else forwardToController[TileCyberBase, Unit](_.closeInventory())

  override def getSizeInventory: Int = if (isController) indInventory.getSizeInventory else forwardToController[TileCyberBase, Int](_.getSizeInventory)

  override def getInventoryStackLimit: Int = if (isController) indInventory.getInventoryStackLimit else forwardToController[TileCyberBase, Int](_.getInventoryStackLimit)

  override def isItemValidForSlot(slot: Int, stack: ItemStack): Boolean = if (isController) indInventory.isItemValidForSlot(slot, stack) else forwardToController[TileCyberBase, Boolean](_.isItemValidForSlot(slot, stack))

  override def getStackInSlotOnClosing(slot: Int): ItemStack = if (isController) indInventory.getStackInSlotOnClosing(slot) else forwardToController[TileCyberBase, ItemStack](_.getStackInSlotOnClosing(slot))

  override def openInventory(): Unit = if (isController) indInventory.openInventory() else forwardToController[TileCyberBase, Unit](_.openInventory())

  override def setInventorySlotContents(slot: Int, stack: ItemStack): Unit = if (isController) indInventory.setInventorySlotContents(slot, stack) else forwardToController[TileCyberBase, Unit](_.setInventorySlotContents(slot, stack))

  override def isUseableByPlayer(player: EntityPlayer): Boolean = if (isController) indInventory.isUseableByPlayer(player) else forwardToController[TileCyberBase, Boolean](_.isUseableByPlayer(player))

  override def getStackInSlot(slot: Int): ItemStack = if (isController) indInventory.getStackInSlot(slot) else forwardToController[TileCyberBase, ItemStack](_.getStackInSlot(slot))

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

  private def topSlotForMachine(machine: MachineMapping): Int = {
    machine.startingSlot + machine.cyberMachine.map(_.getRequiredSlots - 1).getOrElse(0)
  }
}

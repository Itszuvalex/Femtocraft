package com.itszuvalex.femtocraft.core.Cyber.tile

import java.util.Random

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.core.Cyber.item.ItemBaseSeed
import com.itszuvalex.femtocraft.core.Cyber.CyberMachineRegistry
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.{TileFluidTank, MultiBlockComponent}
import com.itszuvalex.itszulib.util.InventoryUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._

import scala.collection.mutable

/**
 * Created by Alex on 27.09.2015.
 */
object TileCyberBase {
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
  def getBaseLocations(size: Int, x: Int, y: Int, z: Int, dim: Int): Set[Loc4] = {
    {
      for {
        bx <- 0 until size
        by <- 0 until baseHeightMap(size)
        bz <- 0 until size
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
  def getSlotLocations(size: Int, x: Int, y: Int, z: Int, dim: Int): Set[Loc4] = {
    {
      for {
        bx <- 0 until size
        by <- baseHeightMap(size) until (baseHeightMap(size) + slotHeightMap(size))
        bz <- 0 until size
      } yield Loc4(x + bx, y + by, z + bz, dim)
    }.toSet
  }

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

class TileCyberBase extends TileEntityBase with MultiBlockComponent with TileMultiblockIndexedInventory with TileFluidTank with IInventory {
  var size: Int                                 = 1
  var machines: Array[String]                   = Array.empty[String]
  var machineSlotMap: mutable.Map[Int, Int]     = mutable.Map.empty[Int, Int]
  var firstFreeSlot: Int                        = 0
  var currentlyBuildingMachine: Int             = -1
  var currentMachineBuildProgress: Int          = 0
  var totalMachineBuildTime: Float              = 500f
  var inProgressData: mutable.Map[String, Any]  = mutable.Map.empty[String, Any]

  override def onSideActivate(player: EntityPlayer, side: Int): Boolean = {
    if (isValidMultiBlock) {
      if (player.isSneaking) {
        if (worldObj.isRemote) return true
        if (isController) Femtocraft.logger.info("Controller") else Femtocraft.logger.info("Not controller")
        return true
      } else {
        //open gui
      }
    }
    false
  }

  def onBlockBreak(): Unit = {
    if (!isValidMultiBlock || worldObj.isRemote) return
    if (isController) {
      machines.zipWithIndex.foreach { machine =>
        CyberMachineRegistry.getMachine(machine._1) match {
          case Some(m) =>
            m.breakMachine(worldObj, xCoord, yCoord + TileCyberBase.baseHeightMap(size) + machineSlotMap(machine._2), zCoord)
          case _ =>
        }
      }
      TileCyberBase.getBaseLocations(size, xCoord, yCoord, zCoord, worldObj.provider.dimensionId).foreach { loc =>
        worldObj.setBlockToAir(loc.x, loc.y, loc.z)
        worldObj.removeTileEntity(loc.x, loc.y, loc.z)
      }
      InventoryUtils.dropItem(ItemBaseSeed.createStack(1, size), worldObj, xCoord + (size / 2), yCoord, zCoord + (size / 2), new Random())
    } else {
      worldObj.getTileEntity(info.x, info.y, info.z) match {
        case base: TileCyberBase => worldObj.setBlockToAir(info.x, info.y, info.z)
        case _ =>
      }
    }
  }

  def buildMachine(name: String): Unit = {
    if (worldObj.isRemote) return
    if (currentlyBuildingMachine > -1) return
    if (!isController) forwardToController[TileCyberBase, Unit](_.buildMachine(name))
    currentlyBuildingMachine = machines.length
    machines(currentlyBuildingMachine) = name
    machineSlotMap(currentlyBuildingMachine) = firstFreeSlot
  }

  override def serverUpdate(): Unit = {
    if (!isController || currentlyBuildingMachine == -1) return
    if (worldObj.getTotalWorldTime % (totalMachineBuildTime / 100) == 0 && currentMachineBuildProgress < 100) {
      currentMachineBuildProgress += 1
      setUpdate()
    }
    if (currentMachineBuildProgress >= 100 && worldObj.getTotalWorldTime >= inProgressData.getOrElseUpdate("targetTime", 0f).asInstanceOf[Float]) {
      CyberMachineRegistry.getMachine(machines(currentlyBuildingMachine)) match {
        case Some(m) =>
          m.formAtBaseAndSlot(worldObj, this, firstFreeSlot)
          firstFreeSlot += m.getRequiredSlots
        case _ =>
      }
      currentlyBuildingMachine = -1
      currentMachineBuildProgress = 0
      inProgressData.clear()
    }
  }

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

  override def defaultInventory: IndexedInventory = new IndexedInventory(9)

  override def hasDescription: Boolean = true

  override def defaultTank: FluidTank = new FluidTank(0)

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = false

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = false
}

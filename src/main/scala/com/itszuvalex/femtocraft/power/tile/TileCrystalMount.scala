package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.item.IPowerCrystal
import com.itszuvalex.femtocraft.power.node._
import com.itszuvalex.femtocraft.power.{ICrystalMount, IPowerPedestal, PowerManager}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.wrappers.{IBattery, PowerBattery}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileInventory
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import com.itszuvalex.itszulib.render.Vector3
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB

import scala.collection.{Set, mutable}

/**
  * Created by Christopher Harris (Itszuvalex) on 8/27/15.
  */
object TileCrystalMount {
  val MOUNT_COMPOUND = "Mount"
  val PEDESTALS_KEY  = "Pedestals"
  val CRYSTAL_KEY    = "Crystal"
  val LOCS_KEY       = "Locs"
  val PEDESTAL_RANGE = 8f
}

class TileCrystalMount extends TileEntityBase with PowerNode with PowerStorageNode with ICrystalMount with TileInventory {
  private val pedestalLocs = mutable.HashSet[Loc4]()

  override def defaultBattery: IBattery = new IBattery {
    override def maxStorage: Double = Option(getCrystalStack).withFilter(_.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).map(_.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).map(_.battery.maxStorage)
      .getOrElse(0d)

    override def clear(): Unit = Option(getCrystalStack).withFilter(_.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).map(_.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).foreach { a => a.battery.storage = 0; setModified() }

    override def copy() = new PowerBattery(storage, maxStorage)

    override def writeToNBT(nbt: NBTTagCompound): Unit = {}

    override def maxStorage_=(max: Double): Unit = Option(getCrystalStack).withFilter(_.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).map(_.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).foreach { a => a.battery.maxStorage = max; setModified() }

    override def storage_=(amt: Double): Unit = Option(getCrystalStack).withFilter(_.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).map(_.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).foreach { a => a.battery.storage = amt; setModified() }

    override def storage: Double = Option(getCrystalStack).withFilter(_.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).map(_.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).map(_.battery.storage)
      .getOrElse(0d)

    override def deserializeNBT(nbt: NBTTagCompound): Unit = {}

    override def serializeNBT() = new NBTTagCompound
  }

  override def storageType: PowerStorageNodeType = PowerStorageNodeType.STORAGE

  override def transferRate: Double = Option(getCrystalStack).withFilter(_.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).map(_.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).map(_.getTransferRate())
    .getOrElse(0d)

  override def powerRadius: Float = TileCrystalMount.PEDESTAL_RANGE

  override def powerTransfer: Double = 100d

  override def rendersPower: Boolean = true

  override def hasGUI = true

  override def getGuiID = GuiIDs.TileCrystalMountGuiID

  override def getMod: AnyRef = Femtocraft

  override def shouldRenderInPass(pass: Int): Boolean = pass == 0 || pass == 1

  /**
    *
    * @return Set of all locations that have pedestal connections.
    */
  override def getPedestalLocations: Set[Loc4] = pedestalLocs

  /**
    *
    * @param loc Location to remove pedestal from.
    */
  override def removePedestal(loc: Loc4): Unit = {
    pedestalLocs -= loc
    setUpdate()
  }


  override def serverUpdate(): Unit = {
    super.serverUpdate()
    if (powerDelegate.network == null && !isInvalid) {
      if (!getCrystalStack.func_190926_b())
        PowerManager.addNode(powerDelegate)
    }

    Option(getCrystalStack).withFilter(_.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).map(_.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).foreach(_.onTick())
  }

  /**
    *
    * @return Crystal ItemStack.  Null if no crystal.
    */
  override def getCrystalStack = getStackInSlot(0)

  override def hasDescription: Boolean = true

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean = {
    slot == 0 && (item == null || item.func_190926_b() || (item.getItem != null && item.getItem.isInstanceOf[IPowerCrystal]))
  }

  override def defaultStorage: ItemStorageArray = new ItemStorageArray(1)

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    var co: NBTTagCompound = null
    if (getCrystalStack != null) {
      co = new NBTTagCompound
      getCrystalStack.writeToNBT(co)
    }
    compound(TileCrystalMount.CRYSTAL_KEY -> co)
    savePedestalLocInfo(compound)
    saveConnectionInfo(compound)
  }

  //  override def setInventorySlotContents(slot: Int, item: ItemStack): Unit = {
  //    item match {
  //      case null =>
  //      case _ => item.getItem match {
  //        case i: IPowerCrystal =>
  //          if (slot == 0 && inventory.getInventory(slot) != null) {
  //            inventory.getInventory(slot) = null
  //            markDirty()
  //          }
  //        case _ =>
  //      }
  //    }
  //    super.setInventorySlotContents(slot, item)
  //  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    setInventorySlotContents(0, compound.NBTCompound(TileCrystalMount.CRYSTAL_KEY)(new ItemStack(_)))
    loadPedestalLocInfo(compound)
    loadConnectionInfo(compound)
    setRenderUpdate()
  }

  def loadPedestalLocInfo(compound: NBTTagCompound): Unit = {
    compound.NBTCompound(TileCrystalMount.MOUNT_COMPOUND) { comp =>
      pedestalLocs.clear()
      pedestalLocs ++= comp.NBTList(TileCrystalMount.PEDESTALS_KEY).map(Loc4(_))
    }
    setRenderUpdate()
  }

  def loadConnectionInfo(compound: NBTTagCompound): Unit = {
    powerDelegate.setRenderLocations(compound.NBTList(TileCrystalMount.LOCS_KEY).map(Loc4(_)).toSet)
    setRenderUpdate()
  }

  override def markDirty(): Unit = {
    super.markDirty()
    if (getWorld.isRemote) return
    setModified()
    setUpdate()
    getCrystalStack match {
      case stack if stack.func_190926_b() =>
        PowerManager.removeNode(powerDelegate)
      case _ => PowerManager.addNode(powerDelegate)
    }
  }

  override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(compound)
    savePedestalLocInfo(compound)
    compound
  }

  def savePedestalLocInfo(compound: NBTTagCompound): NBTTagCompound = {
    compound(
      TileCrystalMount.MOUNT_COMPOUND ->
        NBTCompound(
          TileCrystalMount.PEDESTALS_KEY -> NBTList(pedestalLocs.map(NBTCompound))
        )
    )
  }

  def saveConnectionInfo(compound: NBTTagCompound): NBTTagCompound = {
    compound(
      TileCrystalMount.LOCS_KEY -> NBTList(powerDelegate.renderLocs.map(NBTCompound))
    )
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    loadPedestalLocInfo(compound)
  }

  override def getRenderBoundingBox: AxisAlignedBB = {
    val center = Vector3(getPos.getX + .5f, getPos.getY + .5f, getPos.getZ + .5f)
    new AxisAlignedBB(center.x - powerDelegate.connectionRadius,
      center.y - powerDelegate.connectionRadius,
      center.z - powerDelegate.connectionRadius,
      center.x + powerDelegate.connectionRadius,
      center.y + powerDelegate.connectionRadius,
      center.z + powerDelegate.connectionRadius)
  }

  override def onBlockBreak(): Unit = {
    pedestalLocs.flatMap(_.getTileEntity(true)).collect { case p: IPowerPedestal => p }.foreach(_.setMount(null))
    super.onBlockBreak()
  }

  def onPostBlockPlaced(): Unit = {
    if (getWorld.isRemote) return
    checkAndAddPedestal(EnumFacing.UP)
    checkAndAddPedestal(EnumFacing.DOWN)
  }

  def checkAndAddPedestal(dir: EnumFacing) = {
    getLoc.getOffset(dir).getTileEntity(true) match {
      case Some(i: IPowerPedestal) =>
        if (i.mountLoc == null && i.canSetMount(getLoc) && canAcceptPedestal(getLoc.getOffset(dir))) {
          addPedestal(getLoc.getOffset(dir))
          i.setMount(getLoc)
        }
      case _ =>
    }
  }

  /**
    *
    * @param loc Location to add as pedestal
    */
  override def addPedestal(loc: Loc4): Unit = {
    if (!canAcceptPedestal(loc)) return
    pedestalLocs += loc
    setUpdate()
  }


  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: EnumFacing): Boolean = {
    if (!worldObj.isRemote && par5EntityPlayer.isSneaking) {
      if (powerDelegate.getNetwork != null) {
        PowerManager.removeNode(powerDelegate)
      }
      else {
        PowerManager.addNode(powerDelegate)
      }
    }
    super.onSideActivate(par5EntityPlayer, side)
  }

  /**
    *
    * @param loc Location of pedestal to connect with.
    *
    * @return True if this block can accept a pedestal connection from this location.
    */
  override def canAcceptPedestal(loc: Loc4): Boolean = {
    getLoc.getOffset(EnumFacing.UP) == loc ||
      getLoc.getOffset(EnumFacing.DOWN) == loc
  }

  override def getFieldCount: Int = inventory.getFieldCount

  override def getField(id: Int): Int = inventory.getField(id)

  override def setField(id: Int, value: Int): Unit = inventory.setField(id, value)

  override def func_191420_l(): Boolean = false
}

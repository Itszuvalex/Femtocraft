package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.power.node._
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.wrappers.{IBattery, IItemStack, PowerBattery}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileInventory
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import com.itszuvalex.itszulib.render.Vector3
import com.itszuvalex.itszulib.util.Color
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.AxisAlignedBB

/**
  * Created by Christopher Harris (Itszuvalex) on 8/27/15.
  */
object TileCrystalMount {
  val CRYSTAL_KEY    = "Crystal"
  val LOCS_KEY       = "Locs"
  val PEDESTAL_RANGE = 8f
}

class TileCrystalMount extends TileEntityBase with PowerNode with PowerStorageNode with TileInventory {
  override def defaultBattery: IBattery = new IBattery {
    override def maxStorage: Double = crystalStack.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null).map(_.battery.maxStorage)
      .getOrElse(0d)

    override def clear(): Unit = crystalStack.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null).foreach { a => a.battery.storage = 0; setModified() }

    override def copy() = new PowerBattery(storage, maxStorage)

    override def writeToNBT(nbt: NBTTagCompound): Unit = {}

    override def maxStorage_=(max: Double): Unit = crystalStack.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null).foreach { a => a.battery.maxStorage = max; setModified() }

    override def storage_=(amt: Double): Unit = crystalStack.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null).foreach { a => a.battery.storage = amt; setModified() }

    override def storage: Double = crystalStack.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null).map(_.battery.storage)
      .getOrElse(0d)

    override def deserializeNBT(nbt: NBTTagCompound): Unit = {}

    override def serializeNBT() = new NBTTagCompound
  }

  override def storageType: PowerStorageNodeType = PowerStorageNodeType.STORAGE

  override def transferRate: Double = crystalStack.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null).map(_.getTransferRate())
    .getOrElse(0d)

  override def powerRadius: Float = TileCrystalMount.PEDESTAL_RANGE

  override def powerTransfer: Double = transferRate

  override def rendersPower: Boolean = true

  override def hasGUI = true

  override def getGuiID = GuiIDs.TileCrystalMountGuiID

  override def getMod: AnyRef = Femtocraft

  override def shouldRenderInPass(pass: Int): Boolean = pass == 0 || pass == 1

  override def getColor: Color = crystalStack.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null).map(c => new Color(c.getColor()))
    .getOrElse(super.getColor)


  override def serverUpdate(): Unit = {
    super.serverUpdate()
    if (powerDelegate.network == null && !isInvalid) {
      PowerManager.addNode(powerDelegate)
    }

    crystalStack.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null).foreach(_.onTick())
  }

  /**
    *
    * @return Crystal ItemStack.  Null if no crystal.
    */
  def getCrystalStack: ItemStack = getStackInSlot(0)

  private def crystalStack = storage(0)

  override def hasDescription: Boolean = true

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean = {
    slot == 0 && (item == null || item.func_190926_b() || (item.getItem != null && item.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)))
  }

  override def defaultStorage: ItemStorageArray = new ItemStorageArray(1) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = {
      stack.isEmpty || stack.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
    }
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    var co: NBTTagCompound = null
    if (getCrystalStack != null) {
      co = new NBTTagCompound
      getCrystalStack.writeToNBT(co)
    }
    compound(TileCrystalMount.CRYSTAL_KEY -> co)
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
    loadConnectionInfo(compound)
    setRenderUpdate()
  }

  def loadConnectionInfo(compound: NBTTagCompound): Unit = {
    powerDelegate.setRenderLocations(compound.NBTList(TileCrystalMount.LOCS_KEY).map(Loc4(_)).toSet)
    setRenderUpdate()
  }

  override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(compound)
    compound
  }

  def saveConnectionInfo(compound: NBTTagCompound): NBTTagCompound = {
    compound(
      TileCrystalMount.LOCS_KEY -> NBTList(powerDelegate.renderLocs.map(NBTCompound))
    )
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


  override def getFieldCount: Int = inventory.getFieldCount

  override def getField(id: Int): Int = inventory.getField(id)

  override def setField(id: Int, value: Int): Unit = inventory.setField(id, value)

  override def func_191420_l(): Boolean = false
}

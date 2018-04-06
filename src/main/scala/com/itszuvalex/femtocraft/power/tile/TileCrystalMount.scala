package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.industry.{BatteryEmpty, DynamicIBattery}
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.power.node._
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.wrappers.{IBattery, IItemStack}
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
  var lastCrystal: ItemStack = _

  override def defaultBattery: IBattery = new DynamicIBattery(() => crystalStack.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null).map(_.battery).getOrElse(BatteryEmpty.Empty))

  override def powerStorageNodeType: PowerStorageNodeType = PowerStorageNodeType.STORAGE

  override def powerRadius: Float = TileCrystalMount.PEDESTAL_RANGE

  override def powerTransfer: Double = powerStorageTransferRate

  override def powerStorageTransferRate: Double = crystalStack.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null).map(_.getTransferRate())
    .getOrElse(0d)

  private def crystalStack = storage(0)

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
    val stack = getCrystalStack
    if (stack != lastCrystal)
      setUpdate()
    lastCrystal = stack
  }

  override def hasDescription: Boolean = true

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean = {
    slot == 0 && (item == null || item.isEmpty || (item.getItem != null && item.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)))
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

  /**
    *
    * @return Crystal ItemStack.  Null if no crystal.
    */
  def getCrystalStack: ItemStack = getStackInSlot(0)

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

  def saveConnectionInfo(compound: NBTTagCompound): NBTTagCompound = {
    compound(
      TileCrystalMount.LOCS_KEY -> NBTList(powerDelegate.renderLocs.map(NBTCompound))
    )
  }

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
}

package com.itszuvalex.femtocraft.nanite.tile

import com.itszuvalex.femtocraft.logistics.distributed.{IWorker, IWorkerProvider}
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileIndexedInventory}
import com.itszuvalex.femtocraft.nanite.NaniteHive
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.power.node.{DiffusionNode, IPowerNode}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.{Configurable, Loc4}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileDescriptionPacket
import com.itszuvalex.itszulib.render.Vector3
import com.itszuvalex.itszulib.util.Color
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB

import scala.collection.Set

/**
  * Created by Christopher Harris (Itszuvalex) on 8/25/15.
  */
@Configurable object TileNaniteHiveSmall {
  @Configurable val HIVE_RADIUS    = 20f
                val INVENTORY_SIZE = 30
}

@Configurable class TileNaniteHiveSmall extends TileEntityBase with TileIndexedInventory with DiffusionNode with NaniteHive with IWorkerProvider with TileDescriptionPacket with IInventory {

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    savePowerConnectionInfo(compound)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    loadPowerConnectionInfo(compound)
    setRenderUpdate()
  }

  protected def hiveRadius = TileNaniteHiveSmall.HIVE_RADIUS

  override def getMod: AnyRef = Femtocraft

  override def getGuiID: Int = GuiIDs.NaniteHiveGuiID

  override def hasGUI: Boolean = true

  /**
    *
    * @return Maximum distance children can be from this node, to connect.
    */
  override def childrenConnectionRadius = hiveRadius

  /**
    *
    * @return Distance to accept new tasks when workers complete their own.  Do not pass high values, as this will lead to excessive blank location checking on the order of
    *         (distance/16)&#94;2
    */
  override def getTaskConnectionRadius = hiveRadius

  override def connectionRadius = hiveRadius

  /**
    *
    * @return Set of workers available to be assigned.
    */
  override def getProvidedWorkers: Set[IWorker] = Set()

  /**
    *
    * @return Location of this worker provider, for use in distance calculations.
    */
  override def getProviderLocation: Loc4 = getLoc

  override def hasDescription = true

  /**
    *
    * @return The color of this power node.  This is used for aesthetics.
    */
  override def getColor: Int = {
    if (getParentLoc != null) getParentLoc.getTileEntity(false).orNull match {
      case i: IPowerNode => i.getColor
      case _ => Color(255.toByte, 0, 0, 0).toInt
    }
    else Color(255.toByte, 0, 0, 0).toInt
  }

  override def defaultInventory: IndexedInventory = new IndexedInventory(TileNaniteHiveSmall.INVENTORY_SIZE)

  /* Tile Entity */
  override def validate(): Unit = {
    super.validate()
    if (!worldObj.isRemote) PowerManager.addNode(this)
  }

  override def invalidate(): Unit = {
    super.invalidate()
    if (!worldObj.isRemote) PowerManager.removeNode(this)
  }

  override def getRenderBoundingBox: AxisAlignedBB = {
    val center = Vector3(xCoord + .5f, yCoord + .5f, zCoord + .5f)
    AxisAlignedBB.getBoundingBox(center.x - hiveRadius,
                                 center.y - hiveRadius,
                                 center.z - hiveRadius,
                                 center.x + hiveRadius,
                                 center.y + hiveRadius,
                                 center.z + hiveRadius)
  }

  /**
    *
    * @param child
    * @return True if child was a child of this node, and was successfully removed.
    */
  override def removeChild(child: IPowerNode): Boolean = {
    val ret = super.removeChild(child)
    setUpdate()
    ret
  }


  /**
    *
    * @param child
    * @return True if child is successfully added.
    */
  override def addChild(child: IPowerNode): Boolean = {
    val ret = super.addChild(child)
    setUpdate()
    ret
  }

  /**
    *
    * @param parent Parent being set.
    * @return True if parent is successfully set to input parent.
    */
  override def setParent(parent: IPowerNode): Boolean = {
    val ret = super.setParent(parent)
    setUpdate()
    ret
  }

  override def closeInventory(): Unit = indInventory.closeInventory()

  override def decrStackSize(p_70298_1_ : Int, p_70298_2_ : Int): ItemStack = {
    markDirty()
    indInventory.decrStackSize(p_70298_1_, p_70298_2_)
  }

  override def getSizeInventory: Int = indInventory.getSizeInventory

  override def getInventoryStackLimit: Int = indInventory.getInventoryStackLimit

  override def isItemValidForSlot(p_94041_1_ : Int, p_94041_2_ : ItemStack): Boolean = indInventory.isItemValidForSlot(p_94041_1_, p_94041_2_)

  override def getStackInSlotOnClosing(p_70304_1_ : Int): ItemStack = indInventory.getStackInSlotOnClosing(p_70304_1_)

  override def openInventory(): Unit = indInventory.openInventory()

  override def setInventorySlotContents(p_70299_1_ : Int, p_70299_2_ : ItemStack): Unit = indInventory.setInventorySlotContents(p_70299_1_, p_70299_2_)

  override def isUseableByPlayer(p_70300_1_ : EntityPlayer): Boolean = indInventory.isUseableByPlayer(p_70300_1_)

  override def getStackInSlot(p_70301_1_ : Int): ItemStack = indInventory.getStackInSlot(p_70301_1_)

  override def hasCustomInventoryName: Boolean = indInventory.hasCustomInventoryName

  override def getInventoryName: String = indInventory.getInventoryName
}

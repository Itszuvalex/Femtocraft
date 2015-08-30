package com.itszuvalex.femtocraft.nanite.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.logistics.distributed.{IWorker, IWorkerProvider}
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileIndexedInventory}
import com.itszuvalex.femtocraft.nanite.NaniteHive
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.power.node.{DiffusionNode, IPowerNode}
import com.itszuvalex.itszulib.api.core.{Configurable, Loc4}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.DescriptionPacket
import com.itszuvalex.itszulib.render.Vector3
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB

import scala.collection.Set

/**
 * Created by Christopher Harris (Itszuvalex) on 8/25/15.
 */
@Configurable object TileNaniteHiveSmall {
  @Configurable val HIVE_RADIUS    = 20f
                val INVENTORY_SIZE = 36
}

@Configurable class TileNaniteHiveSmall extends TileEntityBase with TileIndexedInventory with DiffusionNode with NaniteHive with IWorkerProvider with DescriptionPacket {

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    savePowerConnectionInfo(compound)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    loadPowerConnectionInfo(compound)
  }

  protected def hiveRadius = TileNaniteHiveSmall.HIVE_RADIUS

  override def getMod: AnyRef = Femtocraft

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

  override def initializePowerSettings(): Unit = {

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
}

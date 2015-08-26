package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.logistics.distributed.IWorkerProvider
import com.itszuvalex.femtocraft.logistics.storage.item.IIndexedInventory
import com.itszuvalex.femtocraft.power.node.DiffusionNode
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.DescriptionPacket

/**
 * Created by Christopher Harris (Itszuvalex) on 8/25/15.
 */
abstract class TileNaniteHive extends TileEntityBase with IIndexedInventory with DiffusionNode with NaniteHive with IWorkerProvider with DescriptionPacket {

  protected def hiveRadius: Float

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
}

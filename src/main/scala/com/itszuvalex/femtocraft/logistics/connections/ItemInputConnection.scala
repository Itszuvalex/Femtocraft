package com.itszuvalex.femtocraft.logistics.connections

import com.itszuvalex.femtocraft.api.logistics.ConnectionDirection
import com.itszuvalex.itszulib.api.storage.IItemStorage

/**
  *
  * @param flopsRequired Flops required to be 'active'
  * @param stackLimit
  */
class ItemInputConnection(flopsRequired: Double, itemsPerOp: Int, storage: IItemStorage, stackLimit: Int = 64) extends ItemConnection(flopsRequired, itemsPerOp, storage, stackLimit) {

  override def active: Boolean = !isPaused && isEmpty // We need flops until we have uploaded an item into our buffer.

  /**
    *
    * @param flops Amount of flops that can be contributed.
    *
    * @return Amount of FLOPs from FLOPs that are unused.
    */
  override def contributeFlops(flops: Double): Double = {
    val ret = super.contributeFlops(flops)
    if (flopsRemaining <= 0) {
      if (isEmpty) {
        // We received computation because we no longer have an item in our uploaded buffer.
        // Do not reset computation until we have found an item, (unless we want to delay checks until computation completes, as a timeout)
        // This will however put greater computational load on the system and slow down reaction time to new items entering it.
        // Find new item to upload
        storage.indices.filter(i => !storage(i).isEmpty).exists { i =>
          setBuffer(storage.split(i, Math.min(itemsPerOp, stackLimit)).toMinecraft)
          !isEmpty
        }

        // Reset computation, wait till next cycle to check again.
        flopsToGo = flopsRequired
      }
    }
    ret
  }

  override def direction: ConnectionDirection = if (isPaused) super.direction else ConnectionDirection.INPUT

  /**
    * Function to determine whether buffer is empty for purposes of efficient input searching
    *
    * @return True if buffer has the equivalent of 'emptiness'
    */
  override def isEmpty: Boolean = buffer == null || buffer.isEmpty
}

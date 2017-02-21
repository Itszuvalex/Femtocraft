package com.itszuvalex.femtocraft.logistics.connections

import com.itszuvalex.femtocraft.api.logistics.ConnectionDirection
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.wrappers.Converter

/**
  *
  * @param flopsRequired Flops required to be 'active'
  * @param stackLimit
  */
class ItemOutputConnection(flopsRequired: Double, itemsPerOp: Int, storage: IItemStorage, stackLimit: Int = 64) extends ItemConnection(flopsRequired, itemsPerOp, storage, stackLimit) {

  /**
    *
    * @param flops Amount of flops that can be contributed.
    *
    * @return Amount of FLOPs from FLOPs that are unused.
    */
  override def contributeFlops(flops: Double): Double = {
    val ret = super.contributeFlops(flops)
    if (flopsRemaining <= 0) {
      if (!isEmpty) {
        var itemsToTransfer = Math.min(buffer.getCount, itemsPerOp)
        storage.indices.view.filter(storage.canInsert(_, Converter.IItemStackFromItemStack(buffer))).exists { i =>
          val stack = Converter.IItemStackFromItemStack(buffer)
          val newBuf = stack.copy()

          val amt = Math.min(stack.stackSize, itemsToTransfer)
          stack.stackSize = amt

          val remains = storage.insert(i, stack)
          val transfered = amt - (if (remains == null || remains.isEmpty) 0 else remains.stackSize)
          itemsToTransfer -= transfered
          newBuf.stackSize -= transfered
          setBuffer(newBuf.toMinecraft)
          isEmpty || (itemsToTransfer <= 0)
        }

        // We tried to transfer as much as we could.  Clear our amount
        // We won't busy wait like we do for input.
        // Input busy-wait essentially is a onTick replacement to pull the initial itemstack into itself
        // We don't need to do that here.
        flopsToGo = flopsRequired
      }
    }
    ret
  }

  override def direction: ConnectionDirection = if (isPaused) super.direction else ConnectionDirection.OUTPUT
}

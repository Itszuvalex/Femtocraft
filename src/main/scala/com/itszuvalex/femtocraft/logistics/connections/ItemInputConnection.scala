package com.itszuvalex.femtocraft.logistics.connections

import com.itszuvalex.femtocraft.api.logistics.ConnectionDirection
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import net.minecraft.nbt.NBTTagCompound

/**
  * Created by Chris on 2/20/2017.
  */

object ItemInputConnection {
  val UPLOAD_KEY = "upload"
}

/**
  *
  * @param flopsRequired Flops required to be 'active'
  * @param stackLimit
  */
class ItemInputConnection(flopsRequired: Double, itemsPerOp: Int, storage: IItemStorage, stackLimit: Int = 64) extends ItemConnection(flopsRequired, itemsPerOp, storage, stackLimit) {
  var uploadingItem: IItemStack = IItemStack.Empty

  override def active: Boolean = !isPaused && isEmpty // We need flops until we have uploaded an item into our buffer.

  def isUploading: Boolean = uploadingItem != null && !uploadingItem.isEmpty

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
        if (isUploading) {
          setBuffer(uploadingItem.toMinecraft)
          uploadingItem = IItemStack.Empty
        }
        else {
          // We received computation because we no longer have an item in our uploaded buffer.
          // Do not reset computation until we have found an item, (unless we want to delay checks until computation completes, as a timeout)
          // This will however put greater computational load on the system and slow down reaction time to new items entering it.
          // Find new item to upload
          storage.indices.filter(i => !storage(i).isEmpty).exists { i =>
            uploadingItem = storage.split(i, Math.min(itemsPerOp, stackLimit))

            //If we have found an item, reset computation
            flopsToGo = flopsRequired
            true
          }
        }
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

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    super.deserializeNBT(nbt)
    uploadingItem = IItemStack.createFromNBT(nbt.getCompoundTag(ItemInputConnection.UPLOAD_KEY))
  }

  override def serializeNBT(): NBTTagCompound = {
    val nbt = super.serializeNBT()
    nbt.setTag(ItemInputConnection.UPLOAD_KEY, uploadingItem.serializeNBT())
    nbt
  }
}

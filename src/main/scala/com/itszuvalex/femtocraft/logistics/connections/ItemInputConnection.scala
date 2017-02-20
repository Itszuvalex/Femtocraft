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

  /**
    * Active is whether or not this channel is actively performing computation.
    * I.E.  An input channel would be performing computation if it had resources in its buffer.
    * Items: an ItemStack
    * Fluids: Some amount of ml.  (If it is a constant transfer, though I recommend time-dispatch buffering)
    * Nanites: Similar to Fluids
    *
    * @return
    */
  override def active: Boolean = super.active || isUploading // Do not need to receive flops when item is in buffer.  Do need to receive when uploading

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
      if (isEmpty && isUploading) {
        setBuffer(uploadingItem.toMinecraft)
        uploadingItem = IItemStack.Empty
      }
      else {
        // We received computation because we no longer have an item in our uploaded buffer.
        // Reset computation
        flopsToGo = flopsRequired
        // Find new item to upload
        storage.indices.filter(i => !storage(i).isEmpty).exists { i =>
          uploadingItem = storage.split(i, Math.min(itemsPerOp, stackLimit))
          true
        }
      }
    }
    ret
  }

  override def direction: ConnectionDirection = ConnectionDirection.INPUT

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

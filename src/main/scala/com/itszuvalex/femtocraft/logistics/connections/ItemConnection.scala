package com.itszuvalex.femtocraft.logistics.connections

import com.itszuvalex.femtocraft.api.logistics.{ConnectionDirection, IConnection, IResource, LogisticsResourceRegistry}
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.Converter
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

object ItemConnection {
  val FLOPS_KEY   = "flops"
  val BUFFER_KEY  = "buffer"
  val CHANNEL_KEY = "channel"
  val PAUSED_KEY  = "paused"
}

/**
  * Created by Chris on 2/20/2017.
  */
abstract class ItemConnection(var flopsRequired: Double, var itemsPerOp: Int, val storage: IItemStorage, var stackLimit: Int = 64) extends IConnection[ItemStack] with INBTSerializable[NBTTagCompound] {
  var flopsToGo    : Double       = 0d
  var uploadBuffer : IItemStorage = new ItemStorageArray(1)
  var uploadChannel: String       = "default"
  var paused       : Boolean      = true

  override def resource: IResource[ItemStack] = LogisticsResourceRegistry.RESOURCE_ITEMS

  override def channel: String = uploadChannel

  def setChannel(channel: String): Unit = uploadChannel = channel

  /**
    * Active is whether or not this channel is actively performing computation.
    * I.E.  An input channel would be performing computation if it had resources in its buffer.
    * Items: an ItemStack
    * Fluids: Some amount of ml.  (If it is a constant transfer, though I recommend time-dispatch buffering)
    * Nanites: Similar to Fluids
    *
    * @return
    */
  override def active: Boolean = !isPaused && !isEmpty

  override def flopsRemaining: Double = flopsToGo

  override def flopsMaximum: Double = flopsRequired

  override def direction: ConnectionDirection = ConnectionDirection.DISABLED

  def pause(): Unit = paused = true

  def unpause(): Unit = paused = false

  def togglePause(): Unit = if (isPaused) unpause() else pause()

  def isPaused: Boolean = paused

  /**
    *
    * @param flops Amount of flops that can be contributed.
    *
    * @return Amount of FLOPs from FLOPs that are unused.
    */
  override def contributeFlops(flops: Double): Double = {
    val contribute = Math.min(flopsRequired, flops)
    flopsToGo -= contribute
    flops - contribute
  }

  override def buffer: ItemStack = uploadBuffer.head.toMinecraft

  override def setBuffer(a: ItemStack): Unit = uploadBuffer(0) = Converter.IItemStackFromItemStack(a)

  override def canInsert(con: ItemStack): Boolean = !paused && storage.indices.exists(storage.canInsert(_, Converter.IItemStackFromItemStack(con)))

  /**
    *
    * @param t Stack to insert
    *
    * @return Remains of insert that are unused.
    */
  override def insert(t: ItemStack): ItemStack = uploadBuffer.insert(0, Converter.IItemStackFromItemStack(t)).toMinecraft

  /**
    * Function to determine whether buffer is empty for purposes of efficient input searching
    *
    * @return True if buffer has the equivalent of 'emptiness'
    */
  override def isEmpty: Boolean = buffer == null || buffer.isEmpty

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    uploadChannel = nbt.getString(ItemConnection.CHANNEL_KEY)
    flopsToGo = nbt.getDouble(ItemConnection.FLOPS_KEY)
    paused = nbt.getBoolean(ItemConnection.PAUSED_KEY)
    uploadBuffer.deserializeNBT(nbt.getCompoundTag(ItemConnection.BUFFER_KEY))
  }

  override def serializeNBT(): NBTTagCompound = {
    val nbt = new NBTTagCompound
    nbt.setString(ItemConnection.CHANNEL_KEY, channel)
    nbt.setDouble(ItemConnection.FLOPS_KEY, flopsToGo)
    nbt.setTag(ItemConnection.BUFFER_KEY, uploadBuffer.serializeNBT())
    nbt.setBoolean(ItemConnection.PAUSED_KEY, isPaused)
    nbt
  }
}


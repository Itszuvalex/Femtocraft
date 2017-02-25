package com.itszuvalex.femtocraft.logistics.connections

import com.itszuvalex.femtocraft.api.logistics.{ConnectionDirection, IConnection, IResource, LogisticsResourceRegistry}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.{Converter, IItemStack}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.items.CapabilityItemHandler

object ItemConnection {
  val FLOPS_KEY                = "flops"
  val BUFFER_KEY               = "buffer"
  val CHANNEL_KEY              = "channel"
  val PAUSED_KEY               = "paused"
  val CONNECTION_DIRECTION_KEY = "condir"
  val INTERFACE_DIRECTION_KEY  = "intdir"
}

/**
  * Created by Chris on 2/20/2017.
  */
class ItemConnection(val loc: Loc4, val facing: EnumFacing, nbt: NBTTagCompound, var flopsRequired: Double, var itemsPerOp: Int, var stackLimit: Int = 64) extends IConnection[ItemStack] {
  def storage: Option[IItemStorage] = {
    loc.getOffset(facing).getTileEntity(false) match {
      case Some(i: TileEntity) if i.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, interfaceDirection) =>
        Some(Converter.IItemStorageFromIItemHandler(i.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, interfaceDirection)))
      case _ => None
    }
  }


  /**
    *
    * @return Flops generated per tick
    */
  override def passiveFlopGen: Double = flopsMaximum / (20 * 10)

  def flopsToGo: Double = {
    if (!nbt.hasKey(ItemConnection.FLOPS_KEY)) {
      flopsToGo = flopsRequired
    }
    nbt.getDouble(ItemConnection.FLOPS_KEY)
  }

  def flopsToGo_=(d: Double): Unit = nbt.setDouble(ItemConnection.FLOPS_KEY, d)

  def interfaceDirection: EnumFacing = {
    if (!nbt.hasKey(ItemConnection.INTERFACE_DIRECTION_KEY)) {
      interfaceDirection = Option(facing).map(_.getOpposite).getOrElse(EnumFacing.NORTH)
    }
    EnumFacing.VALUES(nbt.getInteger(ItemConnection.INTERFACE_DIRECTION_KEY))
  }

  def interfaceDirection_=(facing: EnumFacing): Unit = {
    nbt.setInteger(ItemConnection.INTERFACE_DIRECTION_KEY, facing.getIndex)
  }

  override def resource: IResource[ItemStack] = LogisticsResourceRegistry.RESOURCE_ITEMS

  override def channel: String = {
    if (!nbt.hasKey(ItemConnection.CHANNEL_KEY)) {
      channel = "default"
    }
    nbt.getString(ItemConnection.CHANNEL_KEY)
  }

  def channel_=(channel: String): Unit = setChannel(channel)

  override def setChannel(channel: String): Unit = {
    nbt.setString(ItemConnection.CHANNEL_KEY, channel)
  }

  /**
    * Active is whether or not this channel is actively performing computation.
    * I.E.  An input channel would be performing computation if it had resources in its buffer.
    * Items: an ItemStack
    * Fluids: Some amount of ml.  (If it is a constant transfer, though I recommend time-dispatch buffering)
    * Nanites: Similar to Fluids
    *
    * @return
    */
  override def active: Boolean = !isPaused &&
    (direction match {
      case ConnectionDirection.INPUT => canAcceptMoreInput
      case _ => !isEmpty
    })

  private def canAcceptMoreInput: Boolean = {
    isEmpty || ibuffer.stackSize < stackLimit
  }

  override def flopsRemaining: Double = flopsToGo

  override def flopsMaximum: Double = flopsRequired

  override def direction: ConnectionDirection = {
    if (!nbt.hasKey(ItemConnection.CONNECTION_DIRECTION_KEY))
      direction = ConnectionDirection.DISABLED

    if (isPaused) ConnectionDirection.DISABLED else ConnectionDirection.valueOf(nbt.getString(ItemConnection.CONNECTION_DIRECTION_KEY))
  }

  override def setDirection(dir: ConnectionDirection): Unit = nbt.setString(ItemConnection.CONNECTION_DIRECTION_KEY, dir.toString)

  def direction_=(dir: ConnectionDirection): Unit = setDirection(dir)

  def pause(): Unit = isPaused = true

  def unpause(): Unit = isPaused = false

  def togglePause(): Unit = if (isPaused) unpause() else pause()

  def isPaused: Boolean = {
    nbt.getBoolean(ItemConnection.PAUSED_KEY)
  }

  def isPaused_=(b: Boolean): Unit = nbt.setBoolean(ItemConnection.PAUSED_KEY, b)

  /**
    *
    * @param flops Amount of flops that can be contributed.
    *
    * @return Amount of FLOPs from FLOPs that are unused.
    */
  override def contributeFlops(flops: Double): Double = {
    val contribute = Math.min(flopsRemaining, flops)
    flopsToGo -= contribute
    val ret = flops - contribute
    if (flopsRemaining <= 0) {
      direction match {
        case ConnectionDirection.DISABLED =>
        case ConnectionDirection.INPUT => inputItem()
        case ConnectionDirection.OUTPUT => outputItem()
        case _ =>
      }
    }

    ret
  }

  private def inputItem(): Unit = {
    if (canAcceptMoreInput) {
      // We received computation because we no longer have an item in our uploaded buffer.
      // Do not reset computation until we have found an item, (unless we want to delay checks until computation completes, as a timeout)
      // This will however put greater computational load on the system and slow down reaction time to new items entering it.
      // Find new item to upload
      var itemsToTransfer = Math.min(itemsPerOp, stackLimit)
      storage.foreach { s =>
        s.indices.exists { i =>
          if (!s(i).isEmpty) {
            val ins = s.split(i, itemsToTransfer).toMinecraft
            val insStart = ins.getCount
            val ret = insert(ins)
            itemsToTransfer -= (insStart - ret.getCount)
          }
          !canAcceptMoreInput || itemsToTransfer <= 0
        }

        // Reset computation, wait till next cycle to check again.
        flopsToGo = flopsRequired
      }
    }
  }

  private def outputItem(): Unit = {
    if (!isEmpty) {
      var itemsToTransfer = Math.min(buffer.getCount, itemsPerOp)
      storage.foreach { s =>
        val ibuf = ibuffer
        s.indices.view.filter(s.canInsert(_, ibuffer)).exists { i =>
          val stack = Converter.IItemStackFromItemStack(buffer)
          val newBuf = stack.copy()

          val amt = Math.min(stack.stackSize, itemsToTransfer)
          stack.stackSize = amt

          val remains = s.insert(i, stack)
          val transfered = amt - (if (remains == null || remains.isEmpty) 0 else remains.stackSize)
          itemsToTransfer -= transfered
          newBuf.stackSize -= transfered
          setIBuffer(newBuf)
          isEmpty || (itemsToTransfer <= 0)
        }

        // We tried to transfer as much as we could.  Clear our amount
        // We won't busy wait like we do for input.
        // Input busy-wait essentially is a onTick replacement to pull the initial itemstack into itself
        // We don't need to do that here.
        flopsToGo = flopsRequired
      }
    }
  }

  override def buffer: ItemStack = ibuffer.toMinecraft

  def ibuffer: IItemStack = IItemStack.createFromNBT(nbt.getCompoundTag(ItemConnection.BUFFER_KEY))

  override def setBuffer(a: ItemStack): Unit = setIBuffer(Converter.IItemStackFromItemStack(a))

  def setIBuffer(a: IItemStack): Unit = nbt.setTag(ItemConnection.BUFFER_KEY, a.serializeNBT())

  override def canInsert(con: ItemStack): Boolean = !isPaused && storage.exists(s => s.indices.exists(s.canInsert(_, Converter.IItemStackFromItemStack(con))))

  /**
    *
    * @param t Stack to insert
    *
    * @return Remains of insert that are unused.
    */
  override def insert(t: ItemStack): ItemStack = {
    val storage = new ItemStorageArray(1) {
      override def maxStackSize(i: Int): Int = stackLimit
    }
    storage(0) = ibuffer
    val ret = storage.insert(0, Converter.IItemStackFromItemStack(t)).toMinecraft
    setIBuffer(storage(0))
    ret
  }

  /**
    * Function to determine whether buffer is empty for purposes of efficient input searching
    *
    * @return True if buffer has the equivalent of 'emptiness'
    */
  override def isEmpty: Boolean = {
    ibuffer.isEmpty
  }
}


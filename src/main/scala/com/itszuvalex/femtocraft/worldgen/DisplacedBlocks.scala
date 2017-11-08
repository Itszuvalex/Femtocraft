package com.itszuvalex.femtocraft.worldgen

import com.itszuvalex.femtocraft.api.worldgen.IDisplacedBlocks
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import net.minecraft.nbt.NBTTagCompound

import scala.collection.mutable.ArrayBuffer

object DisplacedBlocks {
  val StoredBlocksNbtTag = "StoredBlocks"
}

class DisplacedBlocks extends IDisplacedBlocks {
  val storedBlocks = new ArrayBuffer[IItemStack]

  /**
    *
    * @return Raw buffer of stored blocks
    */
  override def blocks: ArrayBuffer[IItemStack] = storedBlocks

  /**
    *
    * @return Total number of blocks stored.
    */
  override def amountStored: Int = storedBlocks.foldLeft(0)(_ + _.stackSize)

  /**
    * Removes amt number of blocks from random blocks stored in the rift
    *
    * @param amt max # of blocks to remove
    *
    * @return
    */
  override def extractRandom(amt: Int) = ???

  /**
    * Add item stacks into the rift
    *
    * @param iItemStack ItemStack to store in the rift
    *
    * @return Remnants of iItemStack not stored.  Usually IItemStack.Empty
    */
  override def store(iItemStack: IItemStack) = ???

  override def deserializeNBT(nbt: NBTTagCompound) = {
    storedBlocks.clear()

  }

  override def serializeNBT() = {
    val nbt = new NBTTagCompound
    nbt
  }
}

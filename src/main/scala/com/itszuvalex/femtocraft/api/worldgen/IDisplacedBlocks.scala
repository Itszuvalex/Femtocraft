package com.itszuvalex.femtocraft.api.worldgen

import com.itszuvalex.itszulib.api.wrappers.IItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

import scala.collection.mutable.ArrayBuffer

trait IDisplacedBlocks extends INBTSerializable[NBTTagCompound] {

  /**
    *
    * @return Raw buffer of stored blocks
    */
  def blocks: ArrayBuffer[IItemStack]

  /**
    *
    * @return Total number of blocks stored.
    */
  def amountStored: Int

  /**
    * Removes amt number of blocks from random blocks stored in the rift
    *
    * @param amt max # of blocks to remove
    * @return
    */
  def extractRandom(amt: Int): ArrayBuffer[IItemStack]

  /**
    *  Add item stacks into the rift
    *
    * @param iItemStack ItemStack to store in the rift
    * @return Remnants of iItemStack not stored.  Usually IItemStack.Empty
    */
  def store(iItemStack: IItemStack): IItemStack
}

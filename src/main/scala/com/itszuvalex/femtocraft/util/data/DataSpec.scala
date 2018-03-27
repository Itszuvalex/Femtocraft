package com.itszuvalex.femtocraft.util.data

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

import scala.collection.mutable.ArrayBuffer

trait DataSpec extends INBTSerializable[NBTTagCompound] {
  val dataSpec = new DataSpecification(ArrayBuffer())

  override def deserializeNBT(nbt: NBTTagCompound): Unit = dataSpec.deserializeNBT(nbt)

  override def serializeNBT(): NBTTagCompound = dataSpec.serializeNBT()
}

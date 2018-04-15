package com.itszuvalex.femtocraft.util.data

import net.minecraft.nbt.NBTBase
import net.minecraftforge.common.util.INBTSerializable

class DataSerializable[NBT <: NBTBase](key: String, val obj: () => INBTSerializable[NBT]) extends KeyedData(key) {
  def this(key: String, obj: INBTSerializable[NBT]) = this(key, () => obj)

  override def deserializeNBT(nbt: NBTBase): Unit = nbt match {
    case a: NBT => obj().deserializeNBT(a)
    case _ =>
  }

  override def serializeNBT(): NBTBase = obj().serializeNBT()
}

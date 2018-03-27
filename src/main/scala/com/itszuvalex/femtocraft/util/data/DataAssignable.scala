package com.itszuvalex.femtocraft.util.data

import net.minecraft.nbt.NBTBase

abstract class DataAssignable[T](key: String, val getter: () => T, val writer: (T) => NBTBase, val assigner: (T) => Unit, val loader: (NBTBase) => Option[T]) extends KeyedData(key) {
  override def deserializeNBT(nbt: NBTBase): Unit = loader(nbt) match {
    case None =>
    case Some(t) => assigner(t)
  }

  override def serializeNBT(): NBTBase = writer(getter())
}

package com.itszuvalex.femtocraft.util.data

import net.minecraft.nbt.NBTBase

class DataLoadable[T](key: String, val getter: () => T, val writer: (T) => NBTBase, val loader: (NBTBase) => Unit) extends KeyedData(key) {
  override def deserializeNBT(nbt: NBTBase): Unit = loader(nbt)

  override def serializeNBT(): NBTBase = writer(getter())
}

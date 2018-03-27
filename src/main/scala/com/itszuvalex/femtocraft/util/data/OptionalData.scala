package com.itszuvalex.femtocraft.util.data

import net.minecraft.nbt.NBTBase

class OptionalData(var pred: () => Boolean, data: KeyedData) extends KeyedData(data.key) {
  override def deserializeNBT(nbt: NBTBase): Unit = data.deserializeNBT(nbt)

  override def serializeNBT(): NBTBase = if (pred()) data.serializeNBT() else null
}

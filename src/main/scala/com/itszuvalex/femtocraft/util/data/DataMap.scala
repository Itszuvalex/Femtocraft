package com.itszuvalex.femtocraft.util.data

import net.minecraft.nbt.{NBTBase, NBTTagCompound}

class DataMap(key: String, val set: Iterable[KeyedData]) extends KeyedData(key) {
  override def deserializeNBT(nbt: NBTBase): Unit = nbt match {
    case a: NBTTagCompound => set.foreach(d => Option(a.getTag(d.key)).foreach(d.deserializeNBT))
    case _ =>
  }

  override def serializeNBT(): NBTBase = {
    val nbt = new NBTTagCompound
    set.foreach(t => nbt.setTag(t.key, t.serializeNBT()))
    nbt
  }
}

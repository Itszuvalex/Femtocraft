package com.itszuvalex.femtocraft.util.data

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

import scala.collection.mutable.ArrayBuffer

class DataSpecification(val set: ArrayBuffer[KeyedData], var onSave: () => Unit = () => Unit, var onLoad: () => Unit = () => Unit)
  extends INBTSerializable[NBTTagCompound] {

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    set.foreach(d => d.deserializeNBT(nbt.getTag(d.key)))
    onLoad()
  }

  override def serializeNBT(): NBTTagCompound = {
    writeToNBT(new NBTTagCompound)
  }

  def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
    set.foreach(t => Option(t.serializeNBT()).foreach(nbt.setTag(t.key, _)))
    onSave()
    nbt
  }

  def +=(k: KeyedData): set.type = set += k

  def ++=(k: Iterable[KeyedData]): set.type = set ++= k

}

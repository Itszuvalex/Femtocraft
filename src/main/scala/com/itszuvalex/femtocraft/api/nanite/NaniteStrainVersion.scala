package com.itszuvalex.femtocraft.api.nanite

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

object NaniteStrainVersion {
  val MAJOR_NBT = "Ma"
  val MINOR_NBT = "Mi"
}

case class NaniteStrainVersion(private var majorVer: Int, private var minorVer: Int) extends INBTSerializable[NBTTagCompound] with Comparable[NaniteStrainVersion] {
  def major: Int = majorVer

  def minor: Int = minorVer

  def bumpMajor(): NaniteStrainVersion = NaniteStrainVersion(major + 1, 0)

  def bumpMinor(): NaniteStrainVersion = NaniteStrainVersion(major, minor + 1)

  override def serializeNBT(): NBTTagCompound = {
    val nbt = new NBTTagCompound
    nbt.setInteger(NaniteStrainVersion.MAJOR_NBT, majorVer)
    nbt.setInteger(NaniteStrainVersion.MINOR_NBT, minorVer)
    nbt
  }

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    majorVer = nbt.getInteger(NaniteStrainVersion.MAJOR_NBT)
    minorVer = nbt.getInteger(NaniteStrainVersion.MINOR_NBT)
  }

  override def compareTo(o: NaniteStrainVersion): Int = {
    major.compareTo(o.major) match {
      case 0 =>
        minor.compareTo(o.minor)
      case c => c
    }
  }
}

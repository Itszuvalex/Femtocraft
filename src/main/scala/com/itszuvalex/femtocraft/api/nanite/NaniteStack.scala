package com.itszuvalex.femtocraft.api.nanite

import com.itszuvalex.femtocraft.api.nanite.NaniteStack._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

/**
  * Created by Chris on 8/18/2016.
  */
object NaniteStack {
  val STRAIN_KEY = "strain"
  val VOL_KEY    = "vol"

  def apply(nbt: NBTTagCompound): NaniteStack = loadFromNBT(nbt)

  def loadFromNBT(nbt: NBTTagCompound): NaniteStack = {
    val ret = NaniteStack(null, 0)
    ret.deserializeNBT(nbt)
    ret
  }
}

case class NaniteStack(private[nanite] var nan: INanite, private[nanite] var vol: Int) extends INBTSerializable[NBTTagCompound] {

  def strain = nan.strain

  def nMol = Option(nanite).map(_.density * volume).getOrElse(0)

  def nanite = nan

  def volume = vol

  def copy() = NaniteStack(nan, vol)

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    nan = NaniteRegistry.getNanite(nbt.String(STRAIN_KEY)).orNull
    vol = nbt.Int(VOL_KEY)
  }

  override def serializeNBT(): NBTTagCompound = NBTCompound(
    STRAIN_KEY -> Option(nan).map(_.strain).orNull,
    VOL_KEY -> vol
  )
}

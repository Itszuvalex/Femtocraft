package com.itszuvalex.femtocraft.api.nanite

import com.itszuvalex.femtocraft.api.nanite.NaniteStackOLD._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

/**
  * Created by Chris on 8/18/2016.
  */
object NaniteStackOLD {
  val STRAIN_KEY = "strain"
  val VOL_KEY    = "vol"

  def apply(nbt: NBTTagCompound): NaniteStackOLD = loadFromNBT(nbt)

  def loadFromNBT(nbt: NBTTagCompound): NaniteStackOLD = {
    val ret = NaniteStackOLD(null, 0)
    ret.deserializeNBT(nbt)
    ret
  }
}

case class NaniteStackOLD(private[nanite] var nan: INaniteOLD, private[nanite] var vol: Int) extends INBTSerializable[NBTTagCompound] {

  def strain = nan.strain

  def nMol = Option(nanite).map(_.density * volume).getOrElse(0)

  def nanite = nan

  def volume = vol

  def copy() = NaniteStackOLD(nan, vol)

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    nan = NaniteRegistryOLD.getNanite(nbt.String(STRAIN_KEY)).orNull
    vol = nbt.Int(VOL_KEY)
  }

  override def serializeNBT(): NBTTagCompound = NBTCompound(
    STRAIN_KEY -> Option(nan).map(_.strain).orNull,
    VOL_KEY -> vol
  )
}

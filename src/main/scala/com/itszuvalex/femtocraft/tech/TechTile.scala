package com.itszuvalex.femtocraft.tech

import com.itszuvalex.itszulib.core.TileEntityCore
import net.minecraft.nbt.NBTTagCompound

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

object TechTile {
  val TECH_COMPOUND_KEY = "techCompound"
}

trait TechTile extends TileEntityCore with ITechTile {
  override val techs: ArrayBuffer[(String, Int)] = ArrayBuffer[(String, Int)]()

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    val techCompound: NBTTagCompound = par1nbtTagCompound.getCompoundTag(TechTile.TECH_COMPOUND_KEY)
    techCompound.getKeySet.foreach { tech =>
      techs += ((tech, techCompound.getInteger(tech)))
    }
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(par1nbtTagCompound)
    val techCompound = new NBTTagCompound
    techs.foreach(f => techCompound.setInteger(f._1, f._2))
    par1nbtTagCompound.setTag(TechTile.TECH_COMPOUND_KEY, techCompound)
    par1nbtTagCompound
  }
}

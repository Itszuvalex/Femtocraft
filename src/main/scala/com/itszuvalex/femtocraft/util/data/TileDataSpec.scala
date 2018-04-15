package com.itszuvalex.femtocraft.util.data

import com.itszuvalex.itszulib.core.TileEntityBase
import net.minecraft.nbt.NBTTagCompound

import scala.collection.mutable.ArrayBuffer

trait TileDataSpec extends TileEntityBase {
  val itemDataSpec        = new DataSpecification(ArrayBuffer())
  val descriptionDataSpec = new DataSpecification(ArrayBuffer())
  val saveDataSpec        = new DataSpecification(ArrayBuffer())

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    saveDataSpec.deserializeNBT(par1nbtTagCompound)
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(par1nbtTagCompound)
    saveDataSpec.writeToNBT(par1nbtTagCompound)
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    descriptionDataSpec.writeToNBT(compound)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    descriptionDataSpec.deserializeNBT(compound)
  }

  override def loadInfoFromItemNBT(compound: NBTTagCompound): Unit = {
    super.loadInfoFromItemNBT(compound)
    itemDataSpec.deserializeNBT(compound)
  }

  override def saveInfoToItemNBT(compound: NBTTagCompound): Unit = {
    super.saveInfoToItemNBT(compound)
    itemDataSpec.writeToNBT(compound)
  }
}

package com.itszuvalex.femtocraft.api.nanite

import net.minecraft.nbt.NBTTagCompound

object NaniteStack {
  def deserializeNaniteStack(tag: NBTTagCompound): INaniteStack = {
    val archetype = NaniteRegistry.getArchetype(tag.getString(NaniteStack.ARCH_NBT)) match {
      case Some(x) => x
      case None => return INaniteStack.Empty
    }
    val strain    = NaniteRegistry.getStrain(archetype.name, tag.getString(NaniteStack.STRAIN_NBT)) match {
      case Some(x) => x
      case None => return INaniteStack.Empty
    }
    val version   = new NaniteStrainVersion(0, 0)
    version.deserializeNBT(tag.getCompoundTag(NaniteStack.VERSION_NBT))
    val amount = tag.getInteger(NaniteStack.AMT_NBT)
    new NaniteStack(archetype, strain, version, amount)
  }

  val ARCH_NBT    = "arch"
  val STRAIN_NBT  = "strain"
  val VERSION_NBT = "v"
  val AMT_NBT     = "amount"
}

class NaniteStack(var archetype: NaniteArchetype, var strain: NaniteStrain, var version: NaniteStrainVersion, var amount: Int) extends INaniteStack {

  override def amountMax: Int = Int.MaxValue

  override def copy(): INaniteStack = new NaniteStack(archetype, strain, version, amount)

  override def serializeNBT(): NBTTagCompound = {
    val nbt = new NBTTagCompound
    nbt.setString(NaniteStack.ARCH_NBT, archetype.name)
    nbt.setString(NaniteStack.STRAIN_NBT, strain.name)
    nbt.setTag(NaniteStack.VERSION_NBT, version.serializeNBT())
    nbt.setInteger(NaniteStack.AMT_NBT, amount)
    nbt
  }

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    archetype = NaniteRegistry.getArchetype(nbt.getString(NaniteStack.ARCH_NBT)) match {
      case Some(x) => x
      case None => NaniteArchetype.Empty
    }
    strain = NaniteRegistry.getStrain(archetype.name, nbt.getString(NaniteStack.STRAIN_NBT)) match {
      case Some(x) => x
      case None => NaniteStrain.Empty
    }
    version.deserializeNBT(nbt.getCompoundTag(NaniteStack.VERSION_NBT))
    amount = nbt.getInteger(NaniteStack.AMT_NBT)
  }
}

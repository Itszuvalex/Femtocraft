package com.itszuvalex.femtocraft.worldgen

import com.itszuvalex.femtocraft.api.worldgen.{IRift, IRiftTrait, RiftTraitRegistry}
import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString}

import scala.collection.mutable

object Rift {
  val LocNbtTag        = "Loc"
  val TraitsNbtTag     = "Traits"
  val defaultStability = 20
  val maxStability     = 100
  val minStability     = 0
}

class Rift(var loc: Loc4) extends IRift {
  private val riftTraits = mutable.ArrayBuffer[IRiftTrait]()

  def addTraits(traits: Iterable[IRiftTrait]): Unit = {
    riftTraits ++= traits.filterNot(riftTraits.contains)
  }

  override def location: Loc4 = loc

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    loc = Loc4(nbt.getCompoundTag(Rift.LocNbtTag))
    riftTraits.clear()
    val traits = nbt.getTagList(Rift.TraitsNbtTag, 10)
    riftTraits ++= (0 until traits.tagCount()).map(traits.getStringTagAt).flatMap(RiftTraitRegistry.getRiftTrait)
  }

  override def serializeNBT(): NBTTagCompound = {
    val nbt = new NBTTagCompound
    nbt.setTag(Rift.LocNbtTag, loc.serializeNBT())
    val traits = new NBTTagList
    riftTraits.map(_.name).map(new NBTTagString(_)).foreach(traits.appendTag)
    nbt.setTag(Rift.TraitsNbtTag, traits)
    nbt
  }

  override def traits: Iterable[IRiftTrait] = riftTraits

  override def stability: Int = Math.max(Rift.minStability, Math.min(Rift.maxStability, riftTraits.foldLeft(Rift.defaultStability)(_ + _.stabilityModifier)))
}

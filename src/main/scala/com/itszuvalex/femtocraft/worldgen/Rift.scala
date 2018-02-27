package com.itszuvalex.femtocraft.worldgen

import com.itszuvalex.femtocraft.api.worldgen.{IRift, IRiftTrait, RiftTraitRegistry}
import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString}

import scala.collection.mutable

object Rift {
  val LocNbtTag    = "Loc"
  val TraitsNbtTag = "Traits"
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
    riftTraits.foreach { t =>
      traits.appendTag(new NBTTagString(t.name))
                       }
    nbt.setTag(Rift.TraitsNbtTag, traits)
    nbt
  }

  override def traits: Iterable[IRiftTrait] = riftTraits
}

package com.itszuvalex.femtocraft.worldgen

import com.itszuvalex.femtocraft.api.worldgen.IRift
import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.nbt.NBTTagCompound

object Rift {
  val DisplacedBlocksNbtTag = "DisplacedBlocks"
  val LocNbtTag             = "DisplacedBlocks"
}

class Rift(var loc: Loc4) extends IRift {
  val blocks = new DisplacedBlocks

  override def location: Loc4 = loc

  override def displacedBlocks: DisplacedBlocks = blocks

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    loc = Loc4(nbt.getCompoundTag(Rift.LocNbtTag))
    blocks.deserializeNBT(nbt.getCompoundTag(Rift.DisplacedBlocksNbtTag))
  }

  override def serializeNBT(): NBTTagCompound = {
    val nbt = new NBTTagCompound
    nbt.setTag(Rift.LocNbtTag, blocks.serializeNBT())
    nbt.setTag(Rift.DisplacedBlocksNbtTag, blocks.serializeNBT())
    nbt
  }
}

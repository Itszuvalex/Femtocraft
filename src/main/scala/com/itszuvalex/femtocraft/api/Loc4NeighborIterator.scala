package com.itszuvalex.femtocraft.api

import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.util.EnumFacing

class Loc4NeighborIterator(loc4: Loc4) extends Iterator[(Loc4, EnumFacing)] {
  var idx = 0

  override def hasNext: Boolean = idx != EnumFacing.VALUES.length

  override def next(): (Loc4, EnumFacing) = {
    val i      = idx
    val facing = EnumFacing.VALUES(i)
    idx += 1
    (loc4.getOffset(facing), facing)
  }
}

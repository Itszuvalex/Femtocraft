package com.itszuvalex.femtocraft.api

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import net.minecraft.util.EnumFacing

class Loc4TileEntityNeighborIterator(loc4: Loc4) extends Iterator[(ITileEntity, EnumFacing)] {
  var idx = 0

  override def hasNext: Boolean = {
    var hasTe = false
    while (!hasTe && idx != EnumFacing.VALUES.length) {
      val offset = loc4.getOffset(EnumFacing.VALUES(idx))
      hasTe = offset.getITileEntity(false).nonEmpty
      if (!hasTe) {idx += 1}
    }
    hasTe
  }

  override def next(): (ITileEntity, EnumFacing) = {
    val offset = loc4.getOffset(EnumFacing.VALUES(idx))
    (offset.getITileEntity(false).orNull, EnumFacing.VALUES(idx))
  }
}

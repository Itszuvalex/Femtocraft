package com.itszuvalex.femtocraft.industry

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.multiblock.MultiBlockInfo
import net.minecraft.tileentity.TileEntity

class MultiblockForwarder[V, T <: TileEntity](val thisobj: T, val info: MultiBlockInfo,
  val loc: () => Loc4,
  val controllerGet: (Option[T]) => V
) {
  def get: V = {
    if (info.isController(loc()))
      controllerGet(Some(thisobj))
    else controllerGet(info.cLoc.getTileEntity(true) match { case None => None; case Some(a: T) => Some(a); case _ => None })
  }
}

package com.itszuvalex.femtocraft.temp

import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import net.minecraft.util.EnumFacing

abstract class TileEntityInternalModule[T <: TileEntityInternalModule[T]] extends TileEntityModule[T] {
  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[T] = _ => Some(this.asInstanceOf[T])
}

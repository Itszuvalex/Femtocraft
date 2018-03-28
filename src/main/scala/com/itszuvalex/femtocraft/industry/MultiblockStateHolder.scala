package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.util.data.{ConditionalData, DataLoadable}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.multiblock.MultiBlockInfo
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.INBTSerializable

object MultiblockStateHolder {

  class DataMultiblockState[S <: INBTSerializable[NBTTagCompound]](key: String, val holder: MultiblockStateHolder[S, _]) extends
    ConditionalData(() => holder.info.isController(holder.loc()),
      new DataLoadable[S](
      key,
      () => holder.getOrElseUpdateState,
      _.serializeNBT(),
      { case t: NBTTagCompound => holder.getOrElseUpdateState.deserializeNBT(t);
      case _ =>
      }))

}

class MultiblockStateHolder[S <: INBTSerializable[NBTTagCompound], T <: TileEntity](
  val thisObj: T,
  val fact: () => S,
  val info: MultiBlockInfo,
  val loc: () => Loc4,
  val getHolder: (T) => MultiblockStateHolder[S, T]) {
  private var state: Option[S] = None

  private def getOrElseUpdateState: S = {
    state match {
      case None => state = Some(fact())
      case Some(_) =>
    }
    state.get
  }

  def get: Option[S] = new MultiblockForwarder[Option[S], T](thisObj, info, loc, { case None => None; case Some(a) => Some(getHolder(a).getOrElseUpdateState) }).get
}

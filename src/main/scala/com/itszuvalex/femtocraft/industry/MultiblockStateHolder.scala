package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.util.data.{ConditionalData, DataLoadable}
import com.itszuvalex.itszulib.api.multiblock.MultiBlockInfo
import com.itszuvalex.itszulib.util.Debug
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.INBTSerializable
import org.apache.logging.log4j.Level

object MultiblockStateHolder {

  class DataMultiblockState[S <: INBTSerializable[NBTTagCompound]](key: String, val holder: MultiblockStateHolder[S, _ <: TileEntity]) extends
    ConditionalData(() => holder.info().isController,
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
                                                                                     val info: () => MultiBlockInfo,
                                                                                     val getHolder: (T) => MultiblockStateHolder[S, T]) {
  private var state: Option[S] = None

  def get: Option[S] =
    if (info().isController) Some(getOrElseUpdateState)
    else
      info().cLoc.getITileEntity(true) match {
        case None => None
        case Some(a: T) => Option(getHolder(a).getOrElseUpdateState)
        case _ => None
      }

  private def getOrElseUpdateState: S = {
    state match {
      case None =>
        state = Some(fact())
        Debug.log(Level.INFO, s"Created MultiblockState $state")
      case Some(_) =>
    }
    state.get
  }

  def hasState: Boolean = state.isDefined
}

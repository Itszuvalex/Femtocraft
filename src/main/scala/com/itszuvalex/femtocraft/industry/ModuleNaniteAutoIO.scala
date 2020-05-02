package com.itszuvalex.femtocraft.industry

import com.itszuvalex._
import com.itszuvalex.femtocraft.industry.ModuleNaniteAutoIO._
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfigurationOLD
import com.itszuvalex.itszulib.api.core.{IModule, Module}
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityInternalModuleTickable
import net.minecraft.nbt.NBTTagCompound

object ModuleNaniteAutoIO {
  val MODULE: IModule[ModuleNaniteAutoIO] = Module.registerModule("ModuleNaniteAutoIO", null)

  val TICKS_DEFAULT = 20
  val AMT_DEFAULT   = 1

  val TICKS_NBT        = "Ticks"
  val TICKS_PER_OP_NBT = "TicksPerOp"
  val AMT_PER_OP_NBT   = "AmtPerOp"
}

class ModuleNaniteAutoIO(val config: SidedNaniteStorageConfigurationOLD, var ticksPerOperation: Int = TICKS_DEFAULT, var amtPerOperation: Int = AMT_DEFAULT) extends TileEntityInternalModuleTickable[ModuleNaniteAutoIO] {
  var ticks = 0

  override def module: IModule[ModuleNaniteAutoIO] = MODULE


  override def serverUpdate(tile: ITileEntity): Unit = {
    ticks = itszulib.util.TileEntityUtils.incrementTicks(ticks, ticksPerOperation)
    femtocraft.util.TileEntityUtils.checkDoNaniteInputIO(tile, config, ticks, amtPerOperation)
    femtocraft.util.TileEntityUtils.checkDoNaniteOutputIO(tile, config, ticks, amtPerOperation)
  }

  override def hasWorldNBT: Boolean = true

  override def writeWorldNBT(tag: NBTTagCompound): Unit = {
    tag.setInteger(TICKS_NBT, ticks)
    tag.setInteger(TICKS_PER_OP_NBT, ticksPerOperation)
    tag.setInteger(AMT_PER_OP_NBT, amtPerOperation)
  }

  override def readWorldNBT(tagCompound: NBTTagCompound): Unit = {
    ticks = tagCompound.getInteger(TICKS_NBT)
    ticksPerOperation = tagCompound.getInteger(TICKS_PER_OP_NBT)
    amtPerOperation = tagCompound.getInteger(AMT_PER_OP_NBT)
  }

}

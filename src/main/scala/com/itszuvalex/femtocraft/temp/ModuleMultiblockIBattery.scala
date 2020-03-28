package com.itszuvalex.femtocraft.temp

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.storage.{DynamicIBattery, IBattery}
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import net.minecraft.util.EnumFacing

class ModuleMultiblockIBattery(val getter: () => Option[IBattery]) extends TileEntityModule[IBattery] {
  val battery: IBattery = new DynamicIBattery(() => getter().getOrElse(IBattery.Empty))

  override def module: IModule[IBattery] = ManagerModules.POWER_STORAGE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IBattery] = _ => getter()
}

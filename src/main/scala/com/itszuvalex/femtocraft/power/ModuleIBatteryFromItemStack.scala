package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.storage.IBattery
import com.itszuvalex.itszulib.api.wrappers.{IItemStack, ITileEntity}
import com.itszuvalex.itszulib.core.TileEntityModule
import net.minecraft.util.EnumFacing

class ModuleIBatteryFromItemStack(stackGetter: () => IItemStack) extends TileEntityModule[IBattery] {
  override def module: IModule[IBattery] = ManagerModules.POWER_STORAGE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IBattery] = _ => stackGetter().moduleOption(ManagerModules.POWER_STORAGE, null)
}

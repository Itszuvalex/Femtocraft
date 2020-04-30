package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityInternalModuleTickable

object ModuleFocusingPedestal {
  val MULTIPLIER = 5
}

class ModuleFocusingPedestal(crystalStorage: IItemStorage) extends TileEntityInternalModuleTickable[ModuleFocusingPedestal] {
  override def module: IModule[ModuleFocusingPedestal] = ???

  override def serverUpdate(tile: ITileEntity): Unit = {
    crystalStorage.head.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null) match {
      case None =>
      case Some(pc) =>
        val bat = pc.battery
        val amt = bat.storage
        bat.fill(pc.getPassiveGen() * ModuleFocusingPedestal.MULTIPLIER)
        if (bat.storage != amt)
          tile.markDirtyForSave()
    }
  }
}

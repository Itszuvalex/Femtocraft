package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.power.item.IPowerCrystal
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import com.itszuvalex.itszulib.util.Color
import net.minecraft.util.EnumFacing

class ModuleColorableFromICrystal(val getter: () => Option[IPowerCrystal]) extends TileEntityModule[Color] {
  override def module: IModule[Color] = ItszuLibModules.COLORABLE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[Color] = _ =>
    getter().map(i => new Color(i.getColor())) match {
      case None =>
        Some(Color(255.toByte, 0.toByte, 0.toByte, 0.toByte))
      case c => c
    }
}

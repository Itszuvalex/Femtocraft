package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.power.IWirelessPowerLeafNode
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import com.itszuvalex.itszulib.util.Color
import net.minecraft.util.EnumFacing

class ModuleColorableFromPowerLeafNode(val leaf: IWirelessPowerLeafNode) extends TileEntityModule[Color] {
  override def module: IModule[Color] = ItszuLibModules.COLORABLE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[Color] = _ =>
    Option(leaf.getParent).flatMap(_.getITileEntity())
                          .withFilter(_.hasModule(ItszuLibModules.COLORABLE, null)).map(_.getModule(ItszuLibModules.COLORABLE, null)) match {
      case None => Some(Color(255.toByte, 0.toByte, 0.toByte, 0.toByte))
      case c => c
    }
}

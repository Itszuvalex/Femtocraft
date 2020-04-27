package com.itszuvalex.femtocraft.power

import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.core.{IModule, Loc4}
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModuleTickable
import com.itszuvalex.itszulib.util.Color
import net.minecraft.util.EnumFacing

class ModuleColorNeighborAverage(checkFacingFunc: EnumFacing => Boolean) extends TileEntityModuleTickable[Color] {
  var color: Color = Color(0, 0, 0, 0)

  override def module: IModule[Color] = ItszuLibModules.COLORABLE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[Color] = _ => Some(color)

  override def clientUpdate(tile: ITileEntity): Unit = {
    var red  : Int = 0
    var green: Int = 0
    var blue : Int = 0
    var numBlocks  = 0
    EnumFacing.VALUES.
              withFilter(checkFacingFunc).
              map(Loc4(tile).getOffset(_)).flatMap(_.getITileEntity(false))
              .flatMap(_.moduleOption(ItszuLibModules.COLORABLE, null))
              .foreach { c =>
                numBlocks += 1
                red += c.red.toInt & 255
                green += c.green.toInt & 255
                blue += c.blue.toInt & 255
              }
    color = if (numBlocks > 0) {
      Color(255.toByte,
            ((red / numBlocks) & 255).toByte,
            ((green / numBlocks) & 255).toByte,
            ((blue / numBlocks) & 255).toByte)

    }
    else {
      Color(0, 0, 0, 0)
    }
  }
}

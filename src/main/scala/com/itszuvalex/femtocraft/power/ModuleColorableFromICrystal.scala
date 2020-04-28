package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.power.item.IPowerCrystal
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModuleTickable
import com.itszuvalex.itszulib.util.Color
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

object ModuleColorableFromICrystal {
  val COLOR_NBT = "c"
}

class ModuleColorableFromICrystal(tile: ITileEntity, val getter: () => Option[IPowerCrystal]) extends TileEntityModuleTickable[Color] {
  var colorOption: Color = Color(0, 0, 0, 0)

  override def module: IModule[Color] = ItszuLibModules.COLORABLE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[Color] = _ => Some(colorOption)

  override def serverUpdate(tile: ITileEntity): Unit = {
    val color: Color = getter().map(i => new Color(i.getColor())) match {
      case None =>
        Color(255.toByte, 0.toByte, 0.toByte, 0.toByte)
      case Some(c) => c
    }

    if (color != colorOption) {
      colorOption = color
      tile.setUpdate()
    }
  }

  override def hasDescriptionNBT: Boolean = true

  override def writeDescriptionNBT(tag: NBTTagCompound): Unit = {
    tag.setInteger(ModuleColorableFromICrystal.COLOR_NBT, colorOption.toInt)
  }

  override def readDescriptionNBT(tag: NBTTagCompound): Unit = {
    colorOption = new Color(tag.getInteger(ModuleColorableFromICrystal.COLOR_NBT))
    tile.setRenderUpdate()
  }
}

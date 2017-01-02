package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace
import com.itszuvalex.itszulib.util.Color
import net.minecraft.util.EnumFacing

/**
  * Created by Chris on 8/23/2016.
  */
class NanoFurnaceRender extends FemtoMachineRender[TileNanoFurnace](Resources.TexBlock("nanofurnace_front.png")) {
  override def getColor(te: TileNanoFurnace): Color = te.getCapability(Capabilities.COLORABLE, EnumFacing.UP)
}

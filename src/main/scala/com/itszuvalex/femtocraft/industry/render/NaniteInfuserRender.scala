package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.industry.tile.TileNaniteInfuser
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.util.Color

/**
  * Created by Chris on 8/23/2016.
  */
class NaniteInfuserRender extends FemtoMachineRender[TileNaniteInfuser](Resources.TexBlock("naniteinfuser_front.png")) {
  override def getColor(te: TileNaniteInfuser): Color = Option(te).withFilter(_.hasCapability(ItszuLibCapabilities.COLORABLE, null)).map(_.getCapability(ItszuLibCapabilities.COLORABLE, null)).getOrElse(Color(255.toByte, 0.toByte, 0.toByte, 0.toByte))
}

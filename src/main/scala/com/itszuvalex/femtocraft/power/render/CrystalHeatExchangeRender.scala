package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.industry.render.FemtoMachineRender
import com.itszuvalex.femtocraft.power.tile.TileCrystalHeatExchanger
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.util.Color

/**
  * Created by Chris on 8/23/2016.
  */
class CrystalHeatExchangeRender extends FemtoMachineRender[TileCrystalHeatExchanger](Resources.TexBlock("crystalheatexchanger_front.png")) {
  override def getColor(te: TileCrystalHeatExchanger): Color = Option(te).withFilter(_.hasCapability(ItszuLibCapabilities.COLORABLE, null)).map(_.getCapability(ItszuLibCapabilities.COLORABLE, null)).getOrElse(Color(255.toByte, 0.toByte, 0.toByte, 0.toByte))
}

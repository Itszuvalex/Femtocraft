package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.render.FemtoMachineRender
import com.itszuvalex.femtocraft.power.tile.TileCrystalChargingArray
import com.itszuvalex.itszulib.util.Color

/**
  * Created by Chris on 8/23/2016.
  */
class CrystalChargingArrayRender extends FemtoMachineRender[TileCrystalChargingArray](Resources.TexBlock("crystalchargingarray_front.png")) {
  override def getColor(te: TileCrystalChargingArray): Color = Option(te).withFilter(_.hasCapability(Capabilities.COLORABLE, null)).map(_.getCapability(Capabilities.COLORABLE, null)).getOrElse(Color(255.toByte, 0.toByte, 0.toByte, 0.toByte))
}

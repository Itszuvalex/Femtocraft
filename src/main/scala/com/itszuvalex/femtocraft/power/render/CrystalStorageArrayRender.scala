package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.industry.render.FemtoMachineRender
import com.itszuvalex.femtocraft.power.tile.TileCrystalStorageArray
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.util.Color

/**
  * Created by Chris on 8/23/2016.
  */
class CrystalStorageArrayRender extends FemtoMachineRender[TileCrystalStorageArray](Resources.TexBlock("crystalstoragearray_front.png")) {
  override def getColor(te: TileCrystalStorageArray): Color = Option(te).withFilter(_.hasCapability(ItszuLibCapabilities.COLORABLE, null)).map(_.getCapability(ItszuLibCapabilities.COLORABLE, null)).getOrElse(Color(255.toByte, 0.toByte, 0.toByte, 0.toByte))
}

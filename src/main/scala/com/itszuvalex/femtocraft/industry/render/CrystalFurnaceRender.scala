package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.industry.tile.{TileCrystalFurnace, TileNanoFurnace}
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.util.Color

/**
  * Created by Chris on 8/23/2016.
  */
class CrystalFurnaceRender extends FemtoMachineRender[TileCrystalFurnace](Resources.TexBlock("nanofurnace_front.png")) {
  override def getColor(te: TileCrystalFurnace): Color = Option(te).map(_.moduleOption(ItszuLibModules.COLORABLE, null).get).getOrElse(Color(255.toByte, 0.toByte, 0.toByte, 0.toByte))
}

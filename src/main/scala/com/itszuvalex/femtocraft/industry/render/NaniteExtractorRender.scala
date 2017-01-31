package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.tile.TileNaniteExtractor
import com.itszuvalex.itszulib.util.Color

/**
  * Created by Chris on 8/23/2016.
  */
class NaniteExtractorRender extends FemtoMachineRender[TileNaniteExtractor](Resources.TexBlock("naniteextractor_front.png")) {
  override def getColor(te: TileNaniteExtractor): Color = Option(te).withFilter(_.hasCapability(Capabilities.COLORABLE, null)).map(_.getCapability(Capabilities.COLORABLE, null)).getOrElse(Color(255.toByte, 0.toByte, 0.toByte, 0.toByte))
}

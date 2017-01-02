package com.itszuvalex.femtocraft.nanite.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteExtractor
import com.itszuvalex.itszulib.util.Color

/**
  * Created by Chris on 8/23/2016.
  */
class NaniteExtractorRender extends FemtoMachineRender[TileNaniteExtractor](Resources.TexBlock("naniteextractor_front.png")) {
  override def getColor(te: TileNaniteExtractor): Color = {
    Option(te).map(_.getParent).flatMap(Option(_)).map(parent => new Color(parent.getColor)).getOrElse(Color(0, 255.toByte, 255.toByte, 255.toByte))
  }
}

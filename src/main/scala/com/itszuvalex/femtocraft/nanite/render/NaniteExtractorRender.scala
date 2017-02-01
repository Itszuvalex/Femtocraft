package com.itszuvalex.femtocraft.nanite.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.render.FemtoMachineRender
import com.itszuvalex.femtocraft.industry.tile.TileNaniteExtractor
import com.itszuvalex.itszulib.util.Color
import net.minecraft.util.EnumFacing

/**
  * Created by Chris on 8/23/2016.
  */
class NaniteExtractorRender extends FemtoMachineRender[TileNaniteExtractor](Resources.TexBlock("naniteextractor_front.png")) {
  override def getColor(te: TileNaniteExtractor): Color = {
    te.getCapability(Capabilities.COLORABLE, EnumFacing.UP)
  }
}

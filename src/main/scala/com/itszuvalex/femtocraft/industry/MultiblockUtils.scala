package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.multiblock.MultiBlockInfo
import net.minecraft.util.EnumFacing

object MultiblockUtils {

  def isLoc4FacingInMultiblock(loc: Loc4, facing: EnumFacing, info: MultiBlockInfo): Boolean = {
    val nloc = loc.getOffset(facing)
    nloc.getTileEntity(false) match {
      case None => false
      case Some(te) if te.hasCapability(Capabilities.TILE_MULTIBLOCK, null) =>
        val multiblock = te.getCapability(Capabilities.TILE_MULTIBLOCK, null)
        multiblock.isController(info.cLoc)
      case _ => false
    }
  }

}

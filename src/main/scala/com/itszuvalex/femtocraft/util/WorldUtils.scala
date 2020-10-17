package com.itszuvalex.femtocraft.util

import com.itszuvalex.femtocraft.industry.tile.TileGerminationChamber
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.IBlock

object WorldUtils {

  def locationsEmptyOrReplaceable(locs: Iterable[Loc4]): Boolean = {
    locs.forall { l =>
      l.getWorld.get.isAirBlock(l.getPos) ||
      l.getBlock(true).get.isReplaceable(l.getWorld.get.toMinecraft, l.getPos)
    }
  }

  def formMultiblockWithLocsAtLoc(locs: Iterable[Loc4], block: IBlock, cLoc: Loc4): Boolean = {
    locs.forall { l =>
      l.getWorld.get.setBlockState(l.getPos, block.toMinecraft.getDefaultState)
      l.getITileEntity(true) match {
        case Some(te: TileGerminationChamber) if te.hasModule(ItszuLibModules.TILE_MULTIBLOCK, null) =>
          te.getModule(ItszuLibModules.TILE_MULTIBLOCK, null).formMultiBlock(l, cLoc)
        case _ => false
      }
    }
  }

  def locationsInBoxFromLoc(loc: Loc4, x: Int, y: Int, z: Int): Iterable[Loc4] = {
    for {
      x <- 0 until x
      y <- 0 until y
      z <- 0 until z
    } yield Loc4(loc.x + x, loc.y + y, loc.z + z, loc.dim)
  }

}

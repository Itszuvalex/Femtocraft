package com.itszuvalex.femtocraft.api

import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import net.minecraft.util.EnumFacing

import scala.collection.mutable

case class NetworkNodeVisitor[C](iter: Iterator[(Loc4, C)], multiBlockDedupe: Boolean = true, dedupeLocs: Boolean = true) {
  var locFilter          : Loc4 => Boolean                                  = (_ => true)
  var neighborFilter     : (C, Loc4, EnumFacing) => Boolean                 = (_, _, _) => true
  var neighborVisitorFunc: PartialFunction[(ITileEntity, EnumFacing), Unit] = {case _ =>}

  lazy val multiblockControllerLocs: mutable.Set[Loc4] = mutable.HashSet[Loc4]()
  lazy val visitedLocs             : mutable.Set[Loc4] = mutable.HashSet[Loc4]()

  def withLocFilter(f: Loc4 => Boolean): NetworkNodeVisitor[C] = {
    locFilter = f
    this
  }

  def withNeighborFilter(f: (C, Loc4, EnumFacing) => Boolean): NetworkNodeVisitor[C] = {
    neighborFilter = f
    this
  }

  def withVisitor(f: PartialFunction[(ITileEntity, EnumFacing), Unit]): NetworkNodeVisitor[C] = {
    neighborVisitorFunc = f
    this
  }

  private def dedupeMultiBlocks(tile: ITileEntity): Boolean = {
    !tile.hasModule(ItszuLibModules.TILE_MULTIBLOCK, null) || {
      val controller = tile.getModule(ItszuLibModules.TILE_MULTIBLOCK, null).controller
      controller.isEmpty || multiblockControllerLocs.add(controller.get)
    }
  }

  private def dedupeLoc(loc: Loc4): Boolean = visitedLocs.add(loc)

  def visit(): Unit = iter.filter(p => locFilter(p._1)).foreach { x =>
    EnumFacing.VALUES.withFilter(f => neighborFilter(x._2, x._1.getOffset(f), f)).map(f => (x._1.getOffset(f), f)).withFilter(l => !dedupeLocs || dedupeLoc(l._1)).foreach { l =>
      l._1.getITileEntity(false).
       withFilter(!multiBlockDedupe || dedupeMultiBlocks(_)).
       withFilter(neighborVisitorFunc.isDefinedAt(_, l._2)).foreach(neighborVisitorFunc(_, l._2))
    }
  }

}

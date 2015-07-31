package com.itszuvalex.femtocraft.core.Nanites

import com.itszuvalex.femtocraft.core.Initializable
import com.itszuvalex.femtocraft.core.Nanites.Nanite._

import scala.collection.mutable

/**
 * Created by Christopher Harris (Itszuvalex) on 7/3/15.
 */
object NaniteRegistry extends Initializable {


  private val naniteMap = new mutable.HashMap[String, INanite]

  def registerNanite(nanite: INanite): Unit = {
    naniteMap(nanite.getArchetype) = nanite
  }

  def getNanites = naniteMap.values

  def getNanite(arch: String) = naniteMap.get(arch)


  override def preInit() = {
    registerNanite(new Nanite(ARCH_BUILDER, COLOR_BUILDER))
    registerNanite(new Nanite(ARCH_RECYCLER, COLOR_RECYCLER))
    registerNanite(new Nanite(ARCH_MAINTAINER, COLOR_MAINTAINER))
    registerNanite(new Nanite(ARCH_PROVIDER, COLOR_PROVIDER))
    registerNanite(new Nanite(ARCH_ENERGIZER, COLOR_ENERGIZER))
  }
}

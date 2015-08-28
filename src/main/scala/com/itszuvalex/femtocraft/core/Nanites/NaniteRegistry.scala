package com.itszuvalex.femtocraft.core.Nanites

import com.itszuvalex.femtocraft.core.Nanites.INanite._

import scala.collection.mutable

/**
 * Created by Christopher Harris (Itszuvalex) on 7/3/15.
 */
object NaniteRegistry {


  private val naniteMap = new mutable.HashMap[String, INanite]

  def registerNanite(nanite: INanite): Unit = {
    naniteMap(nanite.getArchetype) = nanite
  }

  def getNanites = naniteMap.values

  def getNanite(arch: String) = naniteMap.get(arch)


  def preInit() = {
    registerNanite(new Nanite(ARCH_BUILDER, COLOR_BUILDER))
    registerNanite(new Nanite(ARCH_RECYCLER, COLOR_RECYCLER))
    registerNanite(new Nanite(ARCH_MAINTAINER, COLOR_MAINTAINER))
    registerNanite(new Nanite(ARCH_PROVIDER, COLOR_PROVIDER))
    registerNanite(new Nanite(ARCH_ENERGIZER, COLOR_ENERGIZER))
  }
}

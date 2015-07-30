package com.itszuvalex.femtocraft.core.Nanites

import com.itszuvalex.femtocraft.core.Initializable

import scala.collection.mutable

/**
 * Created by Christopher Harris (Itszuvalex) on 7/3/15.
 */
object NaniteRegistry extends Initializable {
  val ARCH_BUILDER     = "Builder"
  val ARCH_RECYCLER    = "Recycler"
  val ARCH_MAINTAINER  = "Maintainer"
  val ARCH_PROVIDER    = "Provider"
  val ARCH_ENERGIZER   = "Energizer"
  val COLOR_BUILDER    = 0
  val COLOR_RECYCLER   = 0
  val COLOR_MAINTAINER = 0
  val COLOR_PROVIDER   = 0
  val COLOR_ENERGIZER  = 0

  private val naniteMap = new mutable.HashMap[String, Nanite]

  def registerNanite(nanite: Nanite): Unit = {
    naniteMap(nanite.archetype) = nanite
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

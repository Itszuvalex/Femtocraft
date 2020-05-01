package com.itszuvalex.femtocraft.api.nanite

import scala.collection.mutable

/**
  * Created by Christopher Harris (Itszuvalex) on 7/3/15.
  */
object NaniteRegistryOLD {
  val NANITE_ARCH_DUMB    = "Dumb"
  val NANITE_DENSITY_DUMB = 1
  val NANITE_DUMB         = new NaniteOLD(NANITE_ARCH_DUMB, NANITE_DENSITY_DUMB)

  private val naniteMap = new mutable.HashMap[String, INaniteOLD]

  def getNanites = naniteMap.values

  def getNanite(arch: String) = naniteMap.get(arch)

  def preInit() = {
    registerNanite(NANITE_DUMB)
  }

  def registerNanite(nanite: INaniteOLD): Unit = {
    naniteMap(nanite.strain) = nanite
  }
}

package com.itszuvalex.femtocraft.nanite

import scala.collection.mutable

/**
  * Created by Christopher Harris (Itszuvalex) on 7/3/15.
  */
object NaniteRegistry {

  private val naniteMap = new mutable.HashMap[String, INanite]

  def getNanites = naniteMap.values

  def getNanite(arch: String) = naniteMap.get(arch)

  def preInit() = {
  }

  def registerNanite(nanite: INanite): Unit = {
    naniteMap(nanite.strain) = nanite
  }
}

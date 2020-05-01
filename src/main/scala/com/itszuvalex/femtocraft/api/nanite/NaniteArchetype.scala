package com.itszuvalex.femtocraft.api.nanite

import scala.collection._

class NaniteArchetype(val name: String) {
  private val strains = new mutable.HashSet[NaniteStrain]()

  def allowedStrains: immutable.Set[NaniteStrain] = strains.toSet

  def addStrain(strain: NaniteStrain): Unit = {
    strains += strain
  }
}

package com.itszuvalex.femtocraft.api.nanite

import scala.collection._

object NaniteRegistry {
  private val archetypes = new mutable.HashSet[NaniteArchetype]()

  def addArchetype(arch: NaniteArchetype): Unit = {
    archetypes += arch
  }

  def addStrain(strain: NaniteStrain): Unit = {
    strain.archetype match {
      case null => throw new IllegalArgumentException(s"NaniteStrains must not have a null archetype: $strain")
      case a =>
        if (!archetypes.contains(a)) throw new IllegalArgumentException(s"Must register NaniteArchetypes before registering strains.  archetype:$a, strain:$strain")
        a.addStrain(strain)
    }
  }

  def allowedArchetypes: immutable.Set[NaniteArchetype] = archetypes.toSet

  def getArchetype(name: String): Option[NaniteArchetype] = archetypes.find(_.name == name)

  def getStrain(archetype: String, strain: String): Option[NaniteStrain] = archetypes.find(_.name == archetype).flatMap(_.allowedStrains.find(_.name == strain))

}

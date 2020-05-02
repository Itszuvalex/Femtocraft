package com.itszuvalex.femtocraft.api.nanite

object NaniteStrain {
  val Empty = new NaniteStrain("Empty", NaniteArchetype.Empty)
}

class NaniteStrain(val name: String, val archetype: NaniteArchetype) {

}

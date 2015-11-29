package com.itszuvalex.femtocraft.nanite.Trait

import scala.collection.mutable

/**
 * Created by Christopher on 7/31/2015.
 */
object TraitRegistry {
  private val traitMap          = mutable.HashMap[String, INaniteTrait]()
  private val classificationMap = mutable.HashMap[String, mutable.HashMap[String, INaniteTrait]]()

  def addTrait(naniteTrait: INaniteTrait) = {
    traitMap.put(naniteTrait.getName, naniteTrait)
    classificationMap.getOrElseUpdate(naniteTrait.getClassification, mutable.HashMap[String, INaniteTrait]()).put(naniteTrait.getName, naniteTrait)
  }

  def getTrait(name: String) = traitMap.get(name)

  def getTraitsOfClassification(classification: String) = classificationMap.get(classification)

}

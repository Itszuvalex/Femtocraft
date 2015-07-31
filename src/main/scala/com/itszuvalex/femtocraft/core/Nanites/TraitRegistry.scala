package com.itszuvalex.femtocraft.core.Nanites

import scala.collection.mutable

/**
 * Created by Christopher on 7/31/2015.
 */
object TraitRegistry {
  private val traitMap          = mutable.HashMap[String, NaniteTrait]()
  private val classificationMap = mutable.HashMap[String, mutable.HashMap[String, NaniteTrait]]()

  def addTrait(naniteTrait: NaniteTrait) = {
    traitMap.put(naniteTrait.name, naniteTrait)
    classificationMap.getOrElseUpdate(naniteTrait.classification, mutable.HashMap[String, NaniteTrait]()).put(naniteTrait.name, naniteTrait)
  }

  def getTrait(name: String) = traitMap.get(name)

  def getTraitsOfClassification(classification: String) = classificationMap.get(classification)

}

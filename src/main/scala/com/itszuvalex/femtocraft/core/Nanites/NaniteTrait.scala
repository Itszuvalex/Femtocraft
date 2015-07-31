package com.itszuvalex.femtocraft.core.Nanites

/**
 * Created by Christopher on 7/31/2015.
 */
object NaniteTrait {
  val CLASSIFICATION_MAJOR = "Major"
  val CLASSIFICATION_MINOR = "Minor"
}

class NaniteTrait(val name: String, val classification: String) extends INaniteTrait {
  override def getName = name

  override def getClassification = classification
}

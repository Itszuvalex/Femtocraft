package com.itszuvalex.femtocraft.nanite.Trait

/**
  * Created by Christopher on 7/31/2015.
  */
object NaniteTrait {
  val CLASSIFICATION_MAJOR = "Major"
  val CLASSIFICATION_MINOR = "Minor"
}

abstract class NaniteTrait(val name: String, val classification: String) extends INaniteTrait {
  override def getName = name

  override def getClassification = classification

}

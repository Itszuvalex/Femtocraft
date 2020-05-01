package com.itszuvalex.femtocraft.api.nanite

/**
  * Created by Christopher Harris (Itszuvalex) on 7/3/15.
  */
class NaniteOLD(private val str: String, private val dens: Int) extends INaniteOLD {
  /**
    * Strain is the identifier for nanites
    *
    * @return
    */
  override def strain: String = str

  /**
    *
    * @return Number of nMols of Nanites per cm3
    */
  override def density: Int = dens
}

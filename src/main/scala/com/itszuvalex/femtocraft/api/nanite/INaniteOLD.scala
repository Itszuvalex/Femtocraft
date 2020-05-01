package com.itszuvalex.femtocraft.api.nanite

/**
  * Created by Christopher Harris (Itszuvalex) on 7/3/15.
  */
object INaniteOLD {

}

trait INaniteOLD {

  /**
    * Strain is the identifier for nanites
    *
    * @return
    */
  def strain: String

  /**
    *
    * @return Number of nMols of Nanites per cm3
    */
  def density: Int

}

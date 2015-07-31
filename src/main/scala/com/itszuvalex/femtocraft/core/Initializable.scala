package com.itszuvalex.femtocraft.core

import com.itszuvalex.femtocraft.Femtocraft

/**
 * Created by Christopher on 7/29/2015.
 */
trait Initializable {
  register()

  private def register() = {
    Femtocraft.initializables += this
  }

  def preInit() = {

  }

  def init() = {

  }

  def postInit() = {

  }

}

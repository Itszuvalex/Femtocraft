package com.itszuvalex.femtocraft.player

import com.itszuvalex.femtocraft.api.nanite.NaniteTank

/**
  * Created by Chris on 8/21/2016.
  */
trait IPlayerNaniteCapability {
  def tank: NaniteTank

  def sync()

}

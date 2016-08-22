package com.itszuvalex.femtocraft.player

import com.itszuvalex.femtocraft.nanite.NaniteTank

/**
  * Created by Chris on 8/21/2016.
  */
trait IPlayerNaniteCapabilities {
  def tank: NaniteTank

  def sync()

}

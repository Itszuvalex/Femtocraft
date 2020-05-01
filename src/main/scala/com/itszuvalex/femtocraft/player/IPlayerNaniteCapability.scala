package com.itszuvalex.femtocraft.player

import com.itszuvalex.femtocraft.api.nanite.NaniteTankOLD

/**
  * Created by Chris on 8/21/2016.
  */
trait IPlayerNaniteCapability {
  def tank: NaniteTankOLD

  def sync()

}

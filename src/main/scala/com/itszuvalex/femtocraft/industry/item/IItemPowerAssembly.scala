package com.itszuvalex.femtocraft.industry.item

import com.itszuvalex.femtocraft.power.item.IPowerStorage

/**
  * Created by Christopher Harris (Itszuvalex) on 2/27/2016.
  */
trait IItemPowerAssembly extends IPowerStorage {

  /**
    *
    * @return Multiplier to return power usage by.
    */
  def getPowerMultipler: Double
}

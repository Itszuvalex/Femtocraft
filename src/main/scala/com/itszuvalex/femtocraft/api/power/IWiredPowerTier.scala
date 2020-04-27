package com.itszuvalex.femtocraft.api.power

object IWiredPowerTier {
  val CRYSTAL: IWiredPowerTier = new IWiredPowerTier {
    override def canConnect(otherTier: IWiredPowerTier): Boolean = otherTier == CRYSTAL
  }
  val DENSE  : IWiredPowerTier = new IWiredPowerTier {
    override def canConnect(otherTier: IWiredPowerTier): Boolean = otherTier == DENSE
  }
  val NANO   : IWiredPowerTier = new IWiredPowerTier {
    override def canConnect(otherTier: IWiredPowerTier): Boolean = otherTier == NANO
  }
}

trait IWiredPowerTier {

  def canConnect(otherTier: IWiredPowerTier): Boolean

}

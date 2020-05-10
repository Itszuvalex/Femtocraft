package com.itszuvalex.femtocraft.api

object IConduitTier {
  val CRYSTAL: IConduitTier = new IConduitTier {
    override def canConnect(otherTier: IConduitTier): Boolean = otherTier == CRYSTAL
  }
  val DENSE  : IConduitTier = new IConduitTier {
    override def canConnect(otherTier: IConduitTier): Boolean = otherTier == DENSE
  }
  val NANO   : IConduitTier = new IConduitTier {
    override def canConnect(otherTier: IConduitTier): Boolean = otherTier == NANO
  }
}

trait IConduitTier {

  def canConnect(otherTier: IConduitTier): Boolean

}

package com.itszuvalex.femtocraft.api.computation

object IComputer {
  val Empty: IComputer = new IComputer {
    override def FLOPsPerTick: Double = 0

    override def useFLOPS(flops: Double): Unit = {}
  }
}

trait IComputer {

  def FLOPsPerTick: Double

  def useFLOPS(flops: Double): Unit

}

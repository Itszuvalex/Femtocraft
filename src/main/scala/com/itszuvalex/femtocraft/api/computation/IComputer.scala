package com.itszuvalex.femtocraft.api.computation

object IComputer {
  val Empty: IComputer = new IComputer {
    override def FLOPsPerTick: Double = 0

    override def useFLOPS(flops: Double): Unit = {}

    /**
     *
     * @return FLOPS / Energy cost per flop.  Higher efficiency computers are used before lower efficiency computers.
     *         This should be dynamic - so throttled computers should return lower if they use generate fewer flops for the same power.
     */
    override def efficiency: Double = Double.MinValue
  }
}

trait IComputer {

  def FLOPsPerTick: Double

  def useFLOPS(flops: Double): Unit

  /**
   *
   * @return FLOPS / Energy cost per flop.  Higher efficiency computers are used before lower efficiency computers.
   *         This should be dynamic - so throttled computers should return lower if they use generate fewer flops for the same power.
   */
  def efficiency: Double

}

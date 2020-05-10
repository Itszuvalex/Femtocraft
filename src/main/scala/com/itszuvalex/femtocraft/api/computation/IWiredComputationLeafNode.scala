package com.itszuvalex.femtocraft.api.computation

trait IWiredComputationLeafNode extends IWiredComputationConnectable {
  def jobs: Seq[IComputationJob]

  def computers: Seq[IComputer]
}

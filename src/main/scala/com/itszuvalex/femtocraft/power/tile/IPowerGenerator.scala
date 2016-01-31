package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.logistics.distributed.{IWorker, IWorkerProvider}

/**
  * Created by Christopher Harris (Itszuvalex) on 1/28/2016.
  */
trait IPowerGenerator extends IWorkerProvider {

  def getCurrentPower: Long

  def getMaximumPower: Long

  /**
    *
    * @param amt     Amount of power to drain
    * @param doDrain False to simulate, true to actually remove power.
    * @return Amount of amt that was successfully drained.
    */
  def drain(amt: Long, doDrain: Boolean): Long

  /**
    *
    * Called by the worker when it attempts to dump power when it detects that this generator has no power.
    *
    * @param worker
    */
  def onDumpNoPower(worker: IWorker): Unit
}

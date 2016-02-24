package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.logistics.distributed.{IWorker, IWorkerProvider}

/**
  * Created by Christopher Harris (Itszuvalex) on 1/28/2016.
  */
trait IPowerGenerator extends IWorkerProvider with ITilePower {

  /**
    *
    * Called by the worker when it attempts to dump power when it detects that this generator has no power.
    *
    * @param worker
    */
  def onDumpNoPower(worker: IWorker): Unit
}

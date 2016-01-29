package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.logistics.distributed.{IWorker, IWorkerProvider}

/**
  * Created by Christopher Harris (Itszuvalex) on 1/28/2016.
  */
trait IPowerGenerator extends IWorkerProvider {

  def onDumpNoPower(worker: IWorker): Unit
}

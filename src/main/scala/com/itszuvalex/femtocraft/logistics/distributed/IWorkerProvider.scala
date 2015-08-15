package com.itszuvalex.femtocraft.logistics.distributed

import com.itszuvalex.itszulib.api.core.Loc4

/**
 * Created by Christopher on 8/15/2015.
 */
trait IWorkerProvider {

  /**
   *
   * @return Set of workers available to be assigned.
   */
  def getProvidedWorkers: Set[IWorker]


  /**
   *
   * @return Location of this worker provider, for use in distance calculations.
   */
  def getLocation: Loc4
}

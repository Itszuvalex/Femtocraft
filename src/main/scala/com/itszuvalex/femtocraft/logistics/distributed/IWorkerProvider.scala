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
  def getProvidedWorkers: scala.collection.Set[IWorker]


  /**
    *
    * @return Location of this worker provider, for use in distance calculations.
    */
  def getProviderLocation: Loc4


  /**
    *
    * @return Distance to accept new tasks when workers complete their own.  Do not pass high values, as this will lead to excessive blank location checking on the order of
    *         (distance/16)&#94;2
    */
  def getTaskConnectionRadius: Float
}

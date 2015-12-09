package com.itszuvalex.femtocraft.logistics.distributed

import com.itszuvalex.itszulib.api.core.Loc4

/**
  * Created by Christopher on 8/15/2015.
  */
trait ITaskProvider {

  /**
    *
    * @return Set of tasks hosted by the provider.
    */
  def getActiveTasks: scala.collection.Set[ITask]

  /**
    *
    * @return Location of this provider, for use in distance calculations.
    */
  def getProviderLocation: Loc4

  /**
    *
    * @return Distance to accept new workers when task completes.  Do not pass high values, as this will lead to excessive blank location checking on the order of
    *         (distance/16)&#94;2
    */
  def getWorkerConnectionRadius: Float

}

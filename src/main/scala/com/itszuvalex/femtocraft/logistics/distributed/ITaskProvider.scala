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
  def getActiveTasks: Set[ITask]

  /**
   *
   * @return Location of this provider, for use in distance calculations.
   */
  def getLocation: Loc4

}

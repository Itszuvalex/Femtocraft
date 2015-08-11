package com.itszuvalex.femtocraft.industry.api

import com.itszuvalex.itszulib.api.core.{Loc4, NBTSerializable}

/**
 * Created by Christopher on 8/10/2015.
 */
trait IJobReceipt extends NBTSerializable {

  def getJobID: String

  def getWorkerLoc: Loc4

}

package com.itszuvalex.femtocraft.industry.api

import com.itszuvalex.itszulib.api.core.{Loc4, NBTSerializable}

/**
 * Created by Christopher on 8/10/2015.
 */
trait IJob[A <: IJobWorker] extends NBTSerializable {
  def getID: String

  def canBegin(worker: A): Boolean

  def tick(worker: A): Unit

  def getRequesterLoc: Loc4

  def isComplete: Boolean

  def getTimeEstimate: Int
}
